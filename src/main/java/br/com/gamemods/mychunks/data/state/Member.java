package br.com.gamemods.mychunks.data.state;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Member
{
    private final PlayerName playerId;
    private Rank rank;

    public Member(PlayerName playerId, Rank rank)
    {
        this.playerId = playerId;
        this.rank = rank;
    }

    public PlayerName getPlayerId()
    {
        return playerId;
    }

    public Rank getRank()
    {
        return rank;
    }

    public void setRank(Rank rank)
    {
        this.rank = rank;
    }
}
