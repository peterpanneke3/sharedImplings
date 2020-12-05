package be.sharedimplings;


import net.runelite.api.NpcID;

import java.util.Optional;

public enum ImplingType {
    DRAGON,
    LUCKY;

    public static Optional<ImplingType> forNpcId(int npcId) {
        switch (npcId) {
            case NpcID.DRAGON_IMPLING:
            case NpcID.DRAGON_IMPLING_1654:
                return Optional.of(DRAGON);
            case NpcID.LUCKY_IMPLING:
            case NpcID.LUCKY_IMPLING_7302:
                return Optional.of(LUCKY);
            default:
                return Optional.empty();
        }
    }

}
