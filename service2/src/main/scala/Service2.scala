import cats.implicits.*
import cats.{Applicative, effect}
import cats.effect.kernel.{Async, Resource}
import cats.effect.{ExitCode, IO, IOApp}
import com.example.protos.hello.{GreeterFs2Grpc, HelloReply, HelloRequest}
import fs2.grpc.syntax.all.*
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import io.grpc.{Grpc, Metadata, Server, ServerServiceDefinition}

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

object Service2 extends IOApp {
  private def app[F[_]: Async]: Resource[F, Server] = for {
    service <- GreeterFs2Grpc.bindServiceResource[F](Service2Impl[F])
    server <- NettyServerBuilder
      .forPort(8088)
      .addService(service)
      .resource[F]
      .map(_.start())
  } yield server

  override def run(args: List[String]): IO[ExitCode] =
    app[IO].use(_ => IO.never.as(ExitCode.Success))
}
