package com.wafflestudio.csereal.common.util

import com.wafflestudio.csereal.common.utils.cleanTextFromHtml
import com.wafflestudio.csereal.common.utils.exchangeValidPageNum
import com.wafflestudio.csereal.common.utils.substringAroundKeyword
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class UtilsTest : BehaviorSpec({
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
                startIdx shouldBe 8
            }

            Then("should return proper substring") {
                result.length shouldBe amount
                result shouldBe " is the awesome test code usin"
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

    Given("Using exchangePageNum to get valid page number") {
        When("Given variables are not positive") {
            val totalMinus = Triple<Int, Int, Long>(1, 1, -1)
            val pageSizeZero = Triple<Int, Int, Long>(0, 1, 1)
            val pageNumZero = Triple<Int, Int, Long>(1, 0, 1)

            Then("should throw AssertionError") {
                shouldThrow<RuntimeException> {
                    exchangeValidPageNum(totalMinus.first, totalMinus.second, totalMinus.third)
                }
                shouldThrow<RuntimeException> {
                    exchangeValidPageNum(pageSizeZero.first, pageSizeZero.second, pageSizeZero.third)
                }
                shouldThrow<RuntimeException> {
                    exchangeValidPageNum(pageNumZero.first, pageNumZero.second, pageNumZero.third)
                }
            }
        }

        When("Given page is in the range") {
            val pageSize = 10
            val total = 100L
            val pageNum = 3

            Then("Should return pageNum itself") {
                val resultPageNum = exchangeValidPageNum(pageSize, pageNum, total)
                resultPageNum shouldBe pageNum
            }
        }

        When("Given page is out of range (bigger)") {
            val pageSize = 10
            val total = 104L
            val pageNum = 15

            Then("Should return last page number") {
                val resultPageNum = exchangeValidPageNum(pageSize, pageNum, total)
                resultPageNum shouldBe 11
            }
        }

        When("Given total count is zero") {
            val pageSize = 10
            val total = 0L
            val pageNum = 1

            Then("Should return first page number") {
                val resultPageNum = exchangeValidPageNum(pageSize, pageNum, total)
                resultPageNum shouldBe 1
            }
        }
    }
})
