package com.example.teretamaapp.room

import kotlinx.serialization.Serializable

val mediaQuery = """
query(${'$'}page: Int, ${'$'}perPage: Int, ${'$'}search: String) {
  Page(page: ${'$'}page, perPage: ${'$'}perPage) {
    pageInfo {
     	hasNextPage
    }
    media(search: ${'$'}search, type: ANIME) {
      id
      title {
        english
        native
        romaji
      }
      coverImage {
        large
      }
      startDate {
        year
      }
      episodes
      type
      studios(isMain: true) {
        nodes {
          name
          isAnimationStudio
        }
      }
    }
  }
}
"""

@Serializable
data class AnilistRequest(
    val query: String,
    val variables: AnilistVariables
)

@Serializable
data class AnilistVariables(
    val page: Int,
    val perPage: Int,
    val search: String
)

@Serializable
data class AnilistResponse(
    val data: AnilistData
) {
    enum class AnilistType { ANIME, MANGA }

    @Serializable
    data class AnilistData(
        val Page: AnilistPage
    )

    @Serializable
    data class AnilistPage(
        val pageInfo: AnilistPageInfo,
        val media: List<AnilistMedia>
    )

    @Serializable
    data class AnilistCoverImage(val large: String)

    @Serializable
    data class AnilistTitle(val english: String?, val native: String?, val romaji: String?)

    @Serializable
    data class AnilistStudios(val nodes: List<AnilistStudioNodes>)

    @Serializable
    data class AnilistStudioNodes(val name: String, val isAnimationStudio: Boolean)

    @Serializable
    data class AnilistPageInfo(val hasNextPage: Boolean)

    @Serializable
    data class AnilistFuzzyDate(val year: Int?)

    @Serializable
    data class AnilistMedia(
        val id: Int,
        val title: AnilistTitle,
        val coverImage: AnilistCoverImage,
        val startDate: AnilistFuzzyDate,
        val episodes: Int?,
        val type: AnilistType,
        val studios: AnilistStudios,
    )
}