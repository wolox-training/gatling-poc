package simulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.util.Random


class Naiofy extends Simulation {

  val httpProtocol = http
    .baseUrl("https://nodejs-qa-training.herokuapp.com")
    .header(name = "Content-Type", value = "application/json")

  val registerFeeder = Iterator.continually(
    Map(
      "email" -> (Random.alphanumeric.take(20).mkString + "@wolox.com.ar"),
      "password" -> Random.alphanumeric.take(10).mkString,
      "firstName" -> Random.alphanumeric.take(20).mkString,
      "lastName" -> Random.alphanumeric.take(20).mkString
    )
  )

  val scn = scenario("Buy an Album") // A scenario is a chain of requests and pauses
      .feed(registerFeeder)
      .exec(http("Register")
      .post("/users")
        .body(StringBody("""{"email": "${email}","password": "${password}", "firstName": "${firstName}", "lastName": "${lastName}"}""")).asJson
      .check(status is 201))

    .exec(http("Login")
      .post("/users/sessions")
      .body(StringBody("""{"email": "${email}","password": "${password}"}""")).asJson
      .check(header("Authorization").saveAs("token"))
      .check(jsonPath("$.user_id").find.saveAs("userId"))
      .check(status is 200))

    .exec(http("User list")
      .get("/users")
      .header(name = "Authorization", value = "${token}")
      .check(status is 200)
    )

    .exec(http("Album list")
      .get("/albums")
      .header(name = "Authorization", value = "${token}")
      .check(status is 200)
    )

    .exec(http("Album photos list")
      .get("/albums/" + new Random().nextInt(100) + "/photos")
      .header(name = "Authorization", value = "${token}")
      .check(status is 200)
    )

    .exec(http("User albums")
      .get("/users/" + "${userId}" + "/albums")
      .header(name = "Authorization", value = "${token}")
      .check(status is 200)
    )

    .exec(http("Buy album")
      .get("/albums/"+ new Random().nextInt(100))
      .header(name = "Authorization", value = "${token}")
      .check(status is 200)
    )

  setUp(scn.inject(atOnceUsers(3)).protocols(httpProtocol))
}
