package com.wafflestudio.csereal.common.util

import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.common.utils.substringAroundKeyword
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class UtilsTest: BehaviorSpec({
    Given("cleanTextFromHtml") {

        When("description is html") {
            val description = """
                <body>
                    <h1>This is a heading</h1>
                    <p>This is a paragraph.</p>
                    <div>
                        <p>This is a paragraph in div.</p>
                    </div>
                    <a href="https://www.naver.com">This is a link</a>
                </body>
            """.trimIndent()

            Then("return text") {
                cleanTextFromHtml(description) shouldBe
                    "This is a heading " +
                    "This is a paragraph. " +
                    "This is a paragraph in div. " +
                    "This is a link"
            }
        }
    }

    Given("substringAroundKeyword") {
        val content = "Hello, World! This is the awesome test code using kotest!"

        When("The keyword is given") {
            val keyword = "awesome"
            val amount = 30

            val (startIdx, result) = substringAroundKeyword(keyword, content, amount)

            Then("should return proper index") {
                startIdx shouldBe 26
            }

            Then("should return proper substring") {
                result.length shouldBe amount
                result shouldBe "d! This is the awesome test co"
            }
        }

        When("Not existing keyword is given") {
            val keyword = "Super Mario"
            val amount = 30

            val (startIdx, result) = substringAroundKeyword(keyword, content, amount)

            Then("should return null to index") {
                startIdx shouldBe null
            }

            Then("should return front substring") {
                result.length shouldBe amount
                result shouldBe "Hello, World! This is the awes"
            }
        }

        When("The amount is too long in left side") {
            val keyword = "World"
            val amount = 30

            val (_, result) = substringAroundKeyword(keyword, content, amount)

            Then("should return front substring") {
                result.length shouldBe amount
                result shouldBe "Hello, World! This is the awes"
            }
        }

        When("The amount is too long in right side") {
            val keyword = "using"
            val amount = 30
            // 12

            val (_, result) = substringAroundKeyword(keyword, content, amount)

            Then("should return back substring") {
                result.length shouldBe amount
                result shouldBe "wesome test code using kotest!"
            }
        }

        When("The amount is longer then full content") {
            val keyword = "is the"
            val amount = 1000

            val (_, result) = substringAroundKeyword(keyword, content, amount)

            Then("should return full content") {
                result shouldBe content
            }
        }
    }
})