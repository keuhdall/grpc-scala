ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

ThisBuild / name := "grpc-scala"

lazy val root = (project in file("."))
  .aggregate(protobuf, service1, service2)

lazy val protobuf = project
  .in(file("protobuf"))
  .settings(name := "protobuf")
  .enablePlugins(Fs2Grpc)

lazy val service1 = project
  .in(file("service1"))
  .settings(
    name := "service1",
    libraryDependencies ++= commonDeps ++ grpcDeps
  )
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(DockerPlugin)
  .dependsOn(protobuf)

lazy val service2 = project
  .in(file("service2"))
  .settings(
    name := "service2",
    dockerExposedPorts ++= Seq(9999),
    libraryDependencies ++= commonDeps ++ grpcDeps
  )
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(DockerPlugin)
  .dependsOn(protobuf)

lazy val commonDeps = Seq(deps.cats, deps.catsEffect)
lazy val grpcDeps = Seq(deps.grpcNetty, deps.grpcServices)

lazy val deps = new {
  val cats = "org.typelevel" %% "cats-core" % "2.9.0"
  val catsEffect = "org.typelevel" %% "cats-effect" % "3.4.6"
  val grpcNetty = "io.grpc" % "grpc-netty-shaded" % "1.53.0"
  val grpcServices = "io.grpc" % "grpc-services" % "1.53.0"
}

PB.protocOptions in Compile := Seq("-xyz")
