package com.ang.Core.Moves;

public class Move {
    public int      from;
    public int      to;
    public MoveFlag     flag;
    public boolean  attack;

    public Move(int from, int to, MoveFlag flag, boolean attack) {
        this.from   = from;
        this.to     = to;
        this.flag   = flag;
        this.attack = attack;
    }

    public Move(int from, int to, boolean attack) {
        this(from, to, MoveFlag.NONE, attack);
    }

    public Move(int from, int to, MoveFlag flag) {
        this(from, to, flag, true);
    }

    public Move(int from, int to) {
        this(from, to, MoveFlag.NONE, true);
    }

    public boolean equals(Move m) {
        return (m.from == from) && (m.to == to);
    }

    public boolean isInvalid() {
        return ((from == -1) || (to == -1));
    }

    public static Move invalid() {
        return new Move(-1, -1);
    }
}
