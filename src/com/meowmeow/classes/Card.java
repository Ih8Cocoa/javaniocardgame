package com.meowmeow.classes;

import java.security.SecureRandom;

/**
 * <h3>Card class</h3>
 * <p>A single Card has the following attributes:</p>
 * <ul>
 *     <li>
 *         A card type: Ace, Two, Three, etc... up to a King, with each of their values vary from 1 to 13
 *     </li>
 *     <li>
 *         A suit - Spades, Clubs, Diamonds or Hearts, with values vary from 1 to 4
 *     </li>
 * </ul>
 */
public class Card {
    private enum Suit {
        SPADES(1), CLUBS(2), DIAMONDS(3), HEARTS(4);

        private int value;

        Suit(int value) {
            this.value = value;
        }
    }

    private enum Type {
        ACE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7),
        EIGHT(8), NINE(9), TEN(10), JACK(11), QUEEN(12), KING(13);

        private int value;
        Type(int value) {
            this.value = value;
        }
    }

    private Type type;
    private Suit suit;

    /**
     * Creates a random card
     */
    public Card() {
        var random = new SecureRandom();
        var allTypes = Type.values();
        var allSuits = Suit.values();
        this.type = allTypes[random.nextInt(10)];
        this.suit = allSuits[random.nextInt(4)];
    }

    /**
     * Creates a predefined card based on the values passed in
     * @param type The type of the card
     * @param suit The card's suit code
     */
    public Card(int type, int suit) {
        var allTypes = Type.values();
        var allSuits = Suit.values();
        this.type = allTypes[type];
        this.suit = allSuits[suit];
    }

    public int getScore() {
        return type.value;
    }

    public int getSuit() {
        return suit.value;
    }

    @Override
    public String toString() {
        return type.toString() + " of " + suit.toString();
    }
}
