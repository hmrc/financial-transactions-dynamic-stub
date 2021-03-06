/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import mocks.{MockDataRepository, MockSchemaValidation}
import models.DataModel
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.mvc.Http.Status

class RequestHandlerControllerSpec extends MockSchemaValidation with MockDataRepository {

  object TestRequestHandlerController extends RequestHandlerController(mockDataRepository, cc)

  lazy val successModel: DataModel = DataModel(
    _id = "test",
    schemaId = "testID1",
    method = "GET",
    status = Status.OK,
    response = None
  )

  lazy val successWithBodyModel: DataModel = DataModel(
    _id = "test",
    schemaId = "testID2",
    method = "GET",
    status = Status.OK,
    response = Some(Json.parse("""{"something" : "hello"}"""))
  )

  "The getRequestHandler method" should {

    "return the status code specified in the model" in {
      lazy val result = TestRequestHandlerController.getRequestHandler("/test")(FakeRequest())
      mockFind(List(successModel))

      status(result) shouldBe Status.OK
    }

    "return the status code and body specified in the model" in {
      lazy val result = TestRequestHandlerController.getRequestHandler("/test")(FakeRequest())
      mockFind(List(successWithBodyModel))

      status(result) shouldBe Status.OK
      await(bodyOf(result)) shouldBe s"${successWithBodyModel.response.get}"
    }

    "return a 404 status when the endpoint cannot be found" in {
      lazy val result = TestRequestHandlerController.getRequestHandler("/test")(FakeRequest())
      mockFind(List())

      status(result) shouldBe Status.NOT_FOUND
      await(bodyOf(result)) shouldBe Json.obj(
        "code" -> "NOT_FOUND",
        "reason" -> "The remote endpoint has indicated that no data can be found."
      ).toString
    }
  }
}
