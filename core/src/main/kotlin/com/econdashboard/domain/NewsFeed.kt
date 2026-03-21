package com.econdashboard.domain

import com.econdashboard.enums.NewsCategory
import jakarta.persistence.*

@Entity
@Table(name = "news_feeds")
class NewsFeed(

    @Column(nullable = false, length = 200)
    var name: String,

    @Column(nullable = false, length = 1000, unique = true)
    var url: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: NewsCategory,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) : BaseEntity()
