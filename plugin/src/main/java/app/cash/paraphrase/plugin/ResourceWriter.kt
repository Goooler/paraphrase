// Copyright Square, Inc.
package app.cash.paraphrase.plugin

import app.cash.paraphrase.plugin.model.MergedResource
import app.cash.paraphrase.plugin.model.MergedResource.Argument
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import java.time.Instant
import kotlin.time.Duration

/**
 * Writes the given tokenized resources to a Kotlin source file.
 */
internal fun writeResources(
  packageName: String,
  mergedResources: List<MergedResource>,
): FileSpec {
  val packageStringsType = ClassName(packageName = packageName, "R", "string")
  val maxVisibility = mergedResources.maxOf { it.visibility }
  return FileSpec.builder(packageName = packageName, fileName = "FormattedResources")
    .addFileComment(
      """
        This code was generated by the Paraphrase Gradle plugin.
        Do not edit this file directly. Instead, edit the string resources in the source file.
      """.trimIndent(),
    )
    .addImport(packageName = packageName, "R")
    .addType(
      TypeSpec.objectBuilder("FormattedResources")
        .apply {
          mergedResources.forEach { mergedResource ->
            addFunction(mergedResource.toFunSpec(packageStringsType))
          }
        }
        .addModifiers(maxVisibility.toKModifier())
        .build(),
    )
    .build()
}

private fun MergedResource.toFunSpec(packageStringsType: TypeName): FunSpec {
  return FunSpec.builder(name.value)
    .apply { if (description != null) addKdoc(description) }
    .apply { arguments.forEach { addParameter(it.toParameterSpec()) } }
    .returns(Types.FormattedResource)
    .apply {
      if (hasContiguousNumberedTokens) {
        addCode(
          buildCodeBlock {
            add("val arguments = arrayOf(⇥\n")
            for (argument in arguments) {
              addStatement("%L,", argument.toParameterCodeBlock())
            }
            add("⇤)\n")
          },
        )
      } else {
        addStatement("val arguments = %T(%L)", Types.ArrayMap.parameterizedBy(STRING, ANY), arguments.size)
        for (argument in arguments) {
          addStatement("arguments.put(%S, %L)", argument.key, argument.toParameterCodeBlock())
        }
      }
    }
    .addCode(
      buildCodeBlock {
        add("return %T(⇥\n", Types.FormattedResource)
        addStatement("id = %T.%L,", packageStringsType, name.value)
        addStatement("arguments = arguments,")
        add("⇤)\n")
      },
    )
    .addModifiers(visibility.toKModifier())
    .build()
}

private fun Argument.toParameterSpec(): ParameterSpec =
  ParameterSpec(
    name = name,
    type = type.asClassName(),
  )

private fun Argument.toParameterCodeBlock(): CodeBlock =
  when (type) {
    Duration::class -> CodeBlock.of("%L.inWholeSeconds", name)
    Instant::class -> CodeBlock.of("%L.toEpochMilli()", name)
    else -> CodeBlock.of("%L", name)
  }

private fun MergedResource.Visibility.toKModifier(): KModifier {
  return when (this) {
    MergedResource.Visibility.Public -> KModifier.PUBLIC
    MergedResource.Visibility.Private -> KModifier.INTERNAL
  }
}

private object Types {
  val ArrayMap = ClassName("android.util", "ArrayMap")
  val FormattedResource = ClassName("app.cash.paraphrase", "FormattedResource")
}