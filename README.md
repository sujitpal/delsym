#DELSYM - An Akka based Content Pipeline

DELSYM is an Actor based Content Ingestion Pipeline. [Actors](http://www.scala-lang.org/old/node/242) allow us to asynchronously distribute work among entities that do discrete peices of this work in a synchronous manner. Delsym's pipeline is similar to that for [Apache Nutch](http://nutch.apache.org/) (more specifically the NutchGORA branch with its dependence on a NoSQL database), but the implementation uses [Akka](http://akka.io/) instead of [Apache Hadoop](http://hadoop.apache.org/).

The architecture diagram for the pipeline is shown below. The central block describes the Actors and Messages, the lower block shows the interactions with various data stores and the upper block represents the HTTP/JSON REST interface the pipeline exposes to clients.

![Pipeline Architecture Diagram](actors.png)

Processing is initiated by a Fetch request sent to the REST API as payload within a HTTP PUT request. The request contains the URL of the page to be processed, the process depth (for crawls), and any fetch level metadata (such as titles and summary from feeds). The REST API transforms this payload into a Fetch message which is sent to the Controller actor. The Controller actor sends it to the Fetch Router Actor which sends it to one of the Fetch Worker Actors. The Fetch Worker downloads the URL and stores the content, along with the depth and metadata if specified, into a [MongoDB](http://www.mongodb.org/) record. Once done, it sends back a FetchComplete message to its router, which causes the Controller to send a Parse message to the Parse Router Actor which forwards it to one of the Parse Worker Actors. The Parse worker parses out the text from the document, extracts specific key value pairs and updating the MongoDB record with this information, and sends back a ParseComplete to its router when done. This causes the Controller to send an Index message to the Index Router Actor which forwards it to one of its Index Worker Actors. The Index Worker Actor retrieves the document from MongoDB and writes it out to an [Apache Solr](http://lucene.apache.org/solr/) instance, and sends back an IndexComplete message back to its router when done. This completes processing for a single document.

For Fetch messages which specify a positive depth, the Controller parses the content for outlinks and creates Fetch messages with these outlinks and depth-1 and sends it to the Fetch router. One can also short circuit the pipeline by only sending a Parse or Index message (for example to fix a bug in these areas), which will cause the document to be processed from that point to completion. It is also fairly easy to add support to enable only Fetch, Index or Parse, if needed.

The pipeline also supports a Stats message which allows clients to find the size of the 3 queues (fetch, parse and index) at any point in time.

To Stop the system, a client sends a Stop message to the pipeline, which will cause its queues to be drained sequentially, and once all queues are drained, the actors are all killed. This is implemented using the Reaper actor which watches the routers for termination. The Stop message is not directly made available via the REST API, but is implemented as a shutdown hook, which is activated when the HTTP server is killed.

To test out the system, run the following commands to download DELSYM and run the HTTP REST server on one terminal:

    git clone https://github.com/sujitpal/delsym.git
    cd delsym
    sbt run

From another terminal, issue the following commands to see the queue sizes:

    curl localhost:8080/stats

Or to send a fetch request (the pipeline is currently configured with mock actors which don't require any of the dependencies listed below):

    curl -X PUT -H "Content-Type: application/json" \
        -d '{"url":"http://www.foo.com/bar", "depth":0, "metadata":{}}' \
        http://localhost:8080/fetch

In production mode, the pipeline expects to find a MongoDB instance and a Solr instance. They are configured using the [application.conf](src/main/resources/application.conf) file. Please set testuser=false for production use.

The MongoDB database and collection must also be created prior to use, as well as the unique index on the URL field. Use the following commands in the mongo shell to do so.

    use delsymdb
    db.documents.ensureIndex({"url": 1}, {unique: true})

The schema.xml for the Solr (4.6) example application already contains all the fields that are necessary for the pipeline to work with existing components. However, if you want to add different fields, make sure that your solrfieldnames and dbfieldnames in application.conf and your Solr schema.xml are updated accordingly.

A slightly more verbose perspective can be found in my blog posts, which I wrote as I was developing the pipeline.

* [Akka Content Ingestion Pipeline, Part I](http://sujitpal.blogspot.com/2013/12/akka-content-ingestion-pipeline-part-i.html)
* [Akka Content Ingestion Pipeline, Part II](http://sujitpal.blogspot.com/2013/12/akka-content-ingestion-pipeline-part-ii.html)
* [Akka Content Ingestion Pipeline, Part III](http://sujitpal.blogspot.com/2013/12/akka-content-ingestion-pipeline-part.html)

