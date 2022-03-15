# reproducing-axo-error
1. Start the application
2. Do a request to localhost:8080. This will output an server sent event stream, emitting 100 random uuid, once per second
3. Shut down Axon Server, application will error with unable to connect to Axon server
4. Do a request to localhost:8080. This will produce an error 500
5. Start Axon Server
6. Do a request to localhost:8080, this will produce an concurrency exception
7. Do another request to localhost:8080, this will produce an AXONIQ-5000 exception.
