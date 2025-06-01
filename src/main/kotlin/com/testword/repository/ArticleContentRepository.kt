package com.testword.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import com.testword.entity.ArticleContent
import com.testword.entity.QArticleContent.articleContent
import com.testword.entity.QMapArticleTag.mapArticleTag
import com.testword.entity.QTag.tag
import org.springframework.data.jpa.repository.JpaRepository

interface ArticleContentRepository : JpaRepository<ArticleContent, Long>, CustomArticleContentRepository {

}

interface CustomArticleContentRepository {
    fun findByTagName(tagName: String): List<ArticleContent>
    fun findByTitleContainingIgnoreCase(title: String): List<ArticleContent>
    fun findByAuthor(author: String): List<ArticleContent>
    fun findByUrl(url: String): ArticleContent?
}

class CustomArticleContentRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : CustomArticleContentRepository {


    override fun findByTagName(tagName: String): List<ArticleContent> {
        val query = jpaQueryFactory
            .selectFrom(articleContent)
            .join(mapArticleTag).on(
                articleContent.id.eq(mapArticleTag.articleId)
            )
            .join(tag).on(
                tag.id.eq(mapArticleTag.tagId)
            )
            .where(tag.name.eq(tagName))

        return query.fetch()
    }

    override fun findByTitleContainingIgnoreCase(title: String): List<ArticleContent> {
        val query = jpaQueryFactory
            .selectFrom(articleContent)
            .where(articleContent.title.lower().like("%${title.lowercase()}%"))
            .orderBy(articleContent.id.desc())

        return query.fetch()
    }

    override fun findByAuthor(author: String): List<ArticleContent> {
        val query = jpaQueryFactory
            .selectFrom(articleContent)
            .where(articleContent.author.eq(author))
            .orderBy(articleContent.id.desc())

        return query.fetch()
    }

    override fun findByUrl(url: String): ArticleContent? {
        val query = jpaQueryFactory
            .selectFrom(articleContent)
            .where(articleContent.url.eq(url))
            .orderBy(articleContent.id.desc())
            .limit(1)

        return query.fetchOne()
    }
}