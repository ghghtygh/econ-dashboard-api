package com.econdashboard.domain

import jakarta.persistence.*

@Entity
@Table(
    name = "subscriptions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "indicator_id"])]
)
class Subscription(
    @Column(name = "user_id", nullable = false)
    var userId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", nullable = false)
    var indicator: Indicator,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) : BaseEntity()
