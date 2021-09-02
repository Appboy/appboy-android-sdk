package com.appboy.sample.util

import android.content.Context
import com.appboy.enums.CardKey
import com.appboy.enums.CardType
import com.appboy.models.cards.Card
import com.braze.Braze
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit

class ContentCardsTestingUtil private constructor() {
    companion object {
        private const val CARD_URL = "https://braze.com"
        private val random = Random()

        fun createRandomCards(context: Context, numCardsOfEachType: Int): List<Card> {
            val cards = mutableListOf<Card>()

            for (cardType in CardType.values()) {
                if (cardType == CardType.DEFAULT) {
                    continue
                }
                repeat((0..numCardsOfEachType).count()) {
                    createRandomCard(context, cardType)?.let { cards.add(it) }
                }
            }

            cards.shuffle()
            return cards
        }

        private fun createRandomCard(context: Context, cardType: CardType): Card? {
            val ccp = CardKey.Provider(true)

            // Set the default fields
            val defaultMapping = mutableMapOf<String, Any>(
                ccp.getKey(CardKey.ID) to getRandomString(),
                ccp.getKey(CardKey.TYPE) to ccp.getServerKeyFromCardType(cardType),
                ccp.getKey(CardKey.VIEWED) to getRandomBoolean(),
                ccp.getKey(CardKey.CREATED) to getNow(),
                ccp.getKey(CardKey.EXPIRES_AT) to getNowPlusDelta(TimeUnit.DAYS, 30),
                ccp.getKey(CardKey.OPEN_URI_IN_WEBVIEW) to getRandomBoolean(),
                ccp.getKey(CardKey.DISMISSED) to false,
                ccp.getKey(CardKey.REMOVED) to false,
                ccp.getKey(CardKey.PINNED) to getRandomBoolean(),
                ccp.getKey(CardKey.DISMISSIBLE) to getRandomBoolean(),
                ccp.getKey(CardKey.IS_TEST) to true
            )

            // Based on the card type, add new fields
            val title = "Title"
            val description = "Description -> cardType $cardType"
            val randomImage = getRandomImageUrl()

            when (cardType) {
                CardType.BANNER -> {
                    defaultMapping.mergeWith(
                        mapOf(
                            ccp.getKey(CardKey.BANNER_IMAGE_IMAGE) to randomImage.first,
                            ccp.getKey(CardKey.BANNER_IMAGE_ASPECT_RATIO) to randomImage.second,
                            ccp.getKey(CardKey.BANNER_IMAGE_URL) to CARD_URL
                        )
                    )
                }
                CardType.CAPTIONED_IMAGE -> {
                    defaultMapping.mergeWith(
                        mapOf(
                            ccp.getKey(CardKey.CAPTIONED_IMAGE_IMAGE) to randomImage.first,
                            ccp.getKey(CardKey.CAPTIONED_IMAGE_ASPECT_RATIO) to randomImage.second,
                            ccp.getKey(CardKey.CAPTIONED_IMAGE_TITLE) to title,
                            ccp.getKey(CardKey.CAPTIONED_IMAGE_DESCRIPTION) to description
                        )
                    )
                    if (random.nextBoolean()) {
                        defaultMapping.mergeWith(
                            mapOf(
                                ccp.getKey(CardKey.CAPTIONED_IMAGE_URL) to CARD_URL
                            )
                        )
                    }
                }
                CardType.SHORT_NEWS -> {
                    defaultMapping.mergeWith(
                        mapOf(
                            ccp.getKey(CardKey.SHORT_NEWS_IMAGE) to randomImage.first,
                            ccp.getKey(CardKey.SHORT_NEWS_TITLE) to title,
                            ccp.getKey(CardKey.SHORT_NEWS_DESCRIPTION) to description
                        )
                    )
                    if (random.nextBoolean()) {
                        defaultMapping.mergeWith(
                            mapOf(
                                ccp.getKey(CardKey.SHORT_NEWS_URL) to CARD_URL
                            )
                        )
                    }
                }
                CardType.TEXT_ANNOUNCEMENT -> {
                    defaultMapping.mergeWith(
                        mapOf(
                            ccp.getKey(CardKey.TEXT_ANNOUNCEMENT_DESCRIPTION) to description,
                            ccp.getKey(CardKey.TEXT_ANNOUNCEMENT_TITLE) to title
                        )
                    )
                    if (random.nextBoolean()) {
                        defaultMapping.mergeWith(
                            mapOf(
                                ccp.getKey(CardKey.TEXT_ANNOUNCEMENT_URL) to CARD_URL
                            )
                        )
                    }
                }
                else -> {
                    // Do nothing!
                }
            }

            val json = JSONObject(defaultMapping.toMap())
            return Braze.getInstance(context).deserializeContentCard(json)
        }

        private fun getRandomString(): String = UUID.randomUUID().toString()

        private fun getRandomBoolean(): Boolean = random.nextBoolean()

        private fun getNow(): Long = getNowPlusDelta(TimeUnit.MILLISECONDS, 0)

        private fun getNowPlusDelta(deltaUnits: TimeUnit, delta: Long): Long = System.currentTimeMillis() + deltaUnits.toMillis(delta)

        private fun getRandomImageUrl(): Pair<String, Double> {
            val height = random.nextInt(500) + 200
            val width = random.nextInt(500) + 200
            return Pair("https://picsum.photos/seed/${System.nanoTime()}/$width/$height", width.toDouble() / height.toDouble())
        }

        /**
         * Merges the content of a target map with another map
         */
        private fun MutableMap<String, Any>.mergeWith(another: Map<String, Any>) {
            for ((key, value) in another) {
                this[key] = value
            }
        }
    }
}
