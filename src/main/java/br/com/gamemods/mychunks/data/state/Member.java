package br.com.gamemods.mychunks.data.state;

import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An association of a rank to a player inside a context. This grants the player all permissions that are granted by the rank.
 */
@ParametersAreNonnullByDefault
@NonnullByDefault
public final class Member
{
    private final PlayerName playerId;
    private final Rank rank;

    /**
     * Construct an immutable player rank assignment
     */
    public Member(PlayerName playerId, Rank rank)
    {
        this.playerId = playerId;
        this.rank = rank;
    }

    /**
     * The player that was assigned
     */
    public PlayerName getPlayerId()
    {
        return playerId;
    }

    /**
     * The assigned rank
     */
    public Rank getRank()
    {
        return rank;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        Member member = (Member) o;

        return playerId.getUniqueId().equals(member.playerId.getUniqueId()) && rank.getUniqueId().equals(member.rank.getUniqueId());
    }

    @Override
    public int hashCode()
    {
        int result = playerId.getUniqueId().hashCode();
        result = 31 * result + rank.getUniqueId().hashCode();
        return result;
    }
}
