# grpc-scala
#### An example app showing how to use the fs2-grpc library

## Understand the code
The example is pretty straightfoward, as we only have:
- A client (Service1)
- A server (Service2)

The request and response are pretty simple aswell:
```proto
// The request message containing the user's name.
message HelloRequest {
  string name = 1;
}

// The response message containing the greetings
message HelloReply {
  string message = 1;
}
```

Finally, the service is defined as followed:
```proto
// The greeting service definition.
service Greeter {
  // Sends a greeting
  rpc SayHello (HelloRequest) returns (HelloReply) {}

  rpc SayHelloStream (stream HelloRequest) returns (stream HelloReply) {}
}
```
And implemented like so in the server (Service2):
```scala
object Service2Impl {
  def apply[F[_]: Applicative]: GreeterFs2Grpc[F, Metadata] =
    new GreeterFs2Grpc[F, Metadata] {
      override def sayHello(request: HelloRequest, ctx: Metadata): F[HelloReply] =
        Applicative[F].pure(HelloReply(s"Hello ${request.name}, hope you're good!"))

      override def sayHelloStream(
          request: fs2.Stream[F, HelloRequest],
          ctx: Metadata
      ): fs2.Stream[F, HelloReply] =
        request.evalMap(req => sayHello(req, ctx))
    }
}
```
