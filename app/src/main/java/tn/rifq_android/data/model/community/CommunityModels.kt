package tn.rifq_android.data.model.community

import com.squareup.moshi.Json

data class Comment(
    @Json(name = "_id") val id: String,
    val userId: String,
    val userName: String,
    val userProfileImage: String? = null,
    val userRole: String? = null,
    val text: String,
    val createdAt: String
)

data class Post(
    @Json(name = "_id") val id: String,
    val userId: String,
    val userName: String,
    val userProfileImage: String? = null,
    val petImage: String,
    val caption: String? = null,
    val createdAt: String,
    val likes: Int = 0,
    val loves: Int = 0,
    val hahas: Int = 0,
    val angries: Int = 0,
    val cries: Int = 0,
    val userReaction: String? = null, // "like", "love", "haha", "angry", "cry", or null
    val comments: List<Comment> = emptyList()
)

data class CreatePostRequest(
    val petImage: String,
    val caption: String? = null
)

data class CreatePostResponse(
    val message: String,
    val post: Post
)

data class PostsResponse(
    val posts: List<Post>,
    val total: Int,
    val page: Int,
    val totalPages: Int
)

data class ReactRequest(
    val reactionType: String // "like" or "haha"
)

data class ReactResponse(
    val message: String,
    val post: Post
)

data class AddCommentRequest(
    val text: String
)

data class AddCommentResponse(
    val message: String,
    val post: Post
)

enum class ReactionType(val value: String, val emoji: String) {
    LIKE("like", "👍"),
    LOVE("love", "❤️"),
    HAHA("haha", "😂"),
    ANGRY("angry", "😠"),
    CRY("cry", "😢")
}
