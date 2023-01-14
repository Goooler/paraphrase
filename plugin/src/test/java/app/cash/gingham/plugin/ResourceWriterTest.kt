// Copyright Square, Inc.
package app.cash.gingham.plugin

import app.cash.gingham.plugin.model.TokenizedResource
import app.cash.gingham.plugin.model.TokenizedResource.Token.NamedToken
import app.cash.gingham.plugin.model.TokenizedResource.Token.NumberedToken
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import org.junit.Test

class ResourceWriterTest {
  @Test
  fun writeResourceWithNamedTokens() {
    TokenizedResource(
      name = "test_named",
      description = "Named Description",
      tokens = listOf(
        NamedToken(name = "first", type = Any::class),
        NamedToken(name = "second", type = Instant::class),
        NamedToken(name = "third", type = Int::class),
        NamedToken(name = "fourth", type = Number::class),
        NamedToken(name = "fifth", type = String::class),
      )
    ).assertFile(
      """
      // This code was generated by the Gingham Gradle plugin.
      // Do not edit this file directly. Instead, edit the string resources in the source file.
      package com.gingham.test

      import android.util.ArrayMap
      import app.cash.gingham.FormattedResource
      import com.gingham.test.R
      import java.time.Instant
      import java.util.Date
      import kotlin.Any
      import kotlin.Int
      import kotlin.Number
      import kotlin.String

      public object FormattedResources {
        /**
         * Named Description
         */
        public fun test_named(
          first: Any,
          second: Instant,
          third: Int,
          fourth: Number,
          fifth: String,
        ): FormattedResource {
          val arguments = ArrayMap<String, Any>(5)
          arguments.put("first", first)
          arguments.put("second", Date.from(second))
          arguments.put("third", third)
          arguments.put("fourth", fourth)
          arguments.put("fifth", fifth)
          return FormattedResource(
            id = R.string.test_named,
            arguments = arguments
          )
        }
      }

      """.trimIndent()
    )
  }

  @Test
  fun writeResourceWithNumberedTokens() {
    TokenizedResource(
      name = "test_numbered",
      description = "Numbered Description",
      tokens = listOf(
        NumberedToken(number = 0, type = Any::class),
        NumberedToken(number = 1, type = Instant::class),
        NumberedToken(number = 2, type = Int::class),
        NumberedToken(number = 3, type = Number::class),
        NumberedToken(number = 4, type = String::class),
      )
    ).assertFile(
      """
      // This code was generated by the Gingham Gradle plugin.
      // Do not edit this file directly. Instead, edit the string resources in the source file.
      package com.gingham.test

      import android.util.ArrayMap
      import app.cash.gingham.FormattedResource
      import com.gingham.test.R
      import java.time.Instant
      import java.util.Date
      import kotlin.Any
      import kotlin.Int
      import kotlin.Number
      import kotlin.String

      public object FormattedResources {
        /**
         * Numbered Description
         */
        public fun test_numbered(
          arg0: Any,
          arg1: Instant,
          arg2: Int,
          arg3: Number,
          arg4: String,
        ): FormattedResource {
          val arguments = ArrayMap<String, Any>(5)
          arguments.put("0", arg0)
          arguments.put("1", Date.from(arg1))
          arguments.put("2", arg2)
          arguments.put("3", arg3)
          arguments.put("4", arg4)
          return FormattedResource(
            id = R.string.test_numbered,
            arguments = arguments
          )
        }
      }

      """.trimIndent()
    )
  }

  private fun TokenizedResource.assertFile(expected: String) {
    assertThat(
      buildString {
        writeResources(
          packageName = "com.gingham.test",
          tokenizedResources = listOf(this@assertFile)
        ).writeTo(this)
      }
    ).isEqualTo(expected)
  }
}
