package crisson.tapir.api

import crisson.tapir.models._

import tapir._
import tapir.json.circe._
import io.circe.generic.auto._

object PetsApi {
    // val listPets : EndpointIO[List[Pet]] = jsonBody[List[Pet]]
    // val api: Endpoint[Unit, Unit, Unit, Nothing] = endpoint.get.in("api" / "v1")

    // val petListing: Endpoint[Unit, String, List[Pet], Nothing] = 
    //     endpoint.get.in("pets").errorOut(stringBody).out(jsonBody[List[Pet]])
}
