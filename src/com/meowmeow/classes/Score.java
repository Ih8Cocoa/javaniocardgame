package com.meowmeow.classes;

import org.jetbrains.annotations.NotNull;

/**
 * <h3>Game Score class</h3>
 * <p>Representing the score system of the game</p>
 * <p>A side's score has 3 attributes: </p>
 * <ul>
 *     <li>The total point of the 3 cards</li>
 *     <li>The total suit point of the 3 cards</li>
 *     <li>The Ace-of-Diamond fallback system</li>
 * </ul>
 *
 * <p>Whether or not a side is the winner would be determined after a maximum amount of 3 stages:</p>
 * <ul>
 *     <li>
 *         <strong>Stage 1:</strong> Which side has the bigger point is the winner.
 *         If both sides have equal points, move to stage 2
 *     </li>
 *     <li>
 *         <strong>Stage 2:</strong> Which side has the bigger suit point is the winner
 *         If both sides have equal points and suit points, move to stage 3
 *     </li>
 *     <li>
 *         <strong>Stage 3:</strong> Which side has the Ace of Diamond card is the winner
 *     </li>
 * </ul>
 * <p>
 *     If both sides have equal points, suit points, and neither sides have the Ace of Diamond,
 *     the game will result in a draw
 * </p>
 */
public class Score implements Comparable<Score> {
    private int point = 0, suitPoint = 0;
    private boolean winFallback = false;

    public int getPoint() {
        return point;
    }

    /**
     * <p>Determine which side is the winner. Returns the followings:</p>
     * <ul>
     *     <li>1 if this side wins</li>
     *     <li>-1 if the opponent wins</li>
     *     <li>0 if it's a draw</li>
     * </ul>
     * @param score the opponent's score object
     * @return the comparison result
     */
    @Override
    public int compareTo(@NotNull Score score) {
        if (point > score.point) {
            return 1;
        }
        if (point < score.point) {
            return -1;
        }
        if (suitPoint > score.suitPoint) {
            return 1;
        }
        if (suitPoint < score.suitPoint) {
            return -1;
        }
        if (winFallback) {
            return 1;
        }
        if (score.winFallback) {
            return -1;
        }
        return 0;
    }

    /**
     * <p>Set the card and update the score as follows:</p>
     * <ul>
     *     <li>Add the card's score to this score. If the score is bigger than 10, store only the last digit</li>
     *     <li>Add the card's suit point to this suit point</li>
     *     <li>Determine if this card is Ace of Diamond. If it is, set the winFallback to true</li>
     * </ul>
     * @param card the input card
     */
    public void setPoint(Card card) {
        this.point += card.getScore();
        this.suitPoint += card.getSuit();
        if (this.point > 10) {
            this.point %= 10;
        }
        final var aceOfDiamond = new Card(1,3);
        if (card.equals(aceOfDiamond)) {
            winFallback = true;
        }
    }
}
