package be.sharedimplings;


import net.runelite.api.NpcID;

import java.util.Optional;

public enum ImplingType {
    DRAGON,
    LUCKY,
    NINJA,
    MAGPIE;

    public static Optional<ImplingType> forNpcId(int npcId) {
        switch (npcId) {
            case NpcID.DRAGON_IMPLING:
            case NpcID.DRAGON_IMPLING_1654:
                return Optional.of(DRAGON);
            case NpcID.LUCKY_IMPLING:
            case NpcID.LUCKY_IMPLING_7302:
                return Optional.of(LUCKY);
            case NpcID.NINJA_IMPLING:
            case NpcID.NINJA_IMPLING_1653:
                return Optional.of(NINJA);
            case NpcID.MAGPIE_IMPLING:
            case NpcID.MAGPIE_IMPLING_1652:
                return Optional.of(MAGPIE);
            default:
                return Optional.empty();
        }
    }

}
