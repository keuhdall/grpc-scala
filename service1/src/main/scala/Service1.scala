import cats.effect.kernel.Async
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits.*
import com.example.protos.hello.{GreeterFs2Grpc, HelloReply, HelloRequest}
import fs2.grpc.syntax.all.*
import io.grpc.Metadata
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder

object Service1 extends IOApp {
  private def app[F[_]: Async]: Resource[F, HelloReply] =
    for {
      channel <- NettyChannelBuilder
        .forAddress("localhost", 8088)
        .usePlaintext()
        .resource[F]
      stub <- GreeterFs2Grpc.stubResource[F](channel)
      response <- Resource.eval(stub.sayHello(HelloRequest("My dood"), new Metadata()))
    } yield response

  override def run(args: List[String]): IO[ExitCode] =
    app[IO].use(resp => IO.println(resp.message) *> IO.pure(ExitCode.Success))
}
