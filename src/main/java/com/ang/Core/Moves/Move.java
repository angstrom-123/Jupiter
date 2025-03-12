package com.ang.Core.Moves;

/**
 * Class for a move from a position to another position
 */
public class Move {
    public int      from;
    public int      to;
    public MoveFlag flag;
    public boolean  attack;

    /**
     * Constructs a move
     * @param from index into a BoardRecord's board[] that the move is from
     * @param to index into a BoardRecord's board[] that the move is to
     * @param flag flag to assign to the move
     * @param attack {@code true} if the move attacks another piece, else {@code false}
     */
    public Move(int from, int to, MoveFlag flag, boolean attack) {
        this.from   = from;
        this.to     = to;
        this.flag   = flag;
        this.attack = attack;
    }

    // additional constructors without optional parameters

    public Move(int from, int to, boolean attack) {
        this(from, to, MoveFlag.NONE, attack);
    }
    public Move(int from, int to, MoveFlag flag) {
        this(from, to, flag, true);
    }
    public Move(int from, int to) {
        this(from, to, MoveFlag.NONE, true);
    }

    /**
     * @param m move to check if equal
     * @return {@code true} if the move is equal to this move, else {@code false}
     */
    public boolean equals(Move m) {
        return (m.from == from) && (m.to == to);
    }

    /**
     * @return {@code true} if this move is invalid, else {@code} false
     */
    public boolean isInvalid() {
        return ((from == -1) || (to == -1));
    }

    /**
     * @return an invalid move (to use as a default)
     */
    public static Move invalid() {
        return new Move(-1, -1);
    }
}
