using System.Text.Json;
using MiAxion.Protocol.Contracts;
using MiAxion.Protocol.Serialization;

namespace MiAxion.Protocol.Tests;

public sealed class QuestContractTests
{
    [Fact]
    public void QueryContinuationRoundTripsAndRejectsNonOpaqueValues()
    {
        var request = new QuestQueryRequest(QuestQueryScope.Active, continuation: "AQ_valid-cursor");
        Assert.Equal(request, ProtocolJson.Deserialize<QuestQueryRequest>(ProtocolJson.Serialize(request)));
        var entries = Enumerable.Range(0, 32).Select(i => new QuestQueryEntry($"q-{i}", "Chapter", "Task", QuestState.Active, 0, 1, 0, false)).ToArray();
        var response = new QuestQueryResponse(entries, 40, true, "AQ_next-cursor");
        Assert.Equal(response.NextContinuation, ProtocolJson.Deserialize<QuestQueryResponse>(ProtocolJson.Serialize(response)).NextContinuation);
        Assert.Throws<ArgumentException>(() => new QuestQueryRequest(QuestQueryScope.Active, continuation: "not a cursor"));
        Assert.Throws<ArgumentException>(() => new QuestQueryResponse([], 0, false, "AQ_cursor"));
    }

    [Fact]
    public void CompleteVisibleDetailRoundTripsWithoutDroppingDescriptionOrRewards()
    {
        var detail = CreateDetail();

        var restored = ProtocolJson.Deserialize<QuestDetailResponse>(
            ProtocolJson.Serialize(detail));

        Assert.Equal(detail.QuestId, restored.QuestId);
        Assert.Equal(
            ["Build a water wheel.", "Connect it with shafts."],
            restored.Description);
        Assert.Single(restored.Objectives);
        Assert.Single(restored.Rewards);
        Assert.Single(restored.Dependencies);
    }

    [Fact]
    public void HiddenDescriptionCannotCarryText()
    {
        Assert.Throws<ArgumentException>(() => new QuestDetailResponse(
            "quest-1", "Chapter", "Title", "",
            QuestDescriptionVisibility.HiddenUntilComplete,
            ["secret"], QuestState.Available, 0, false, false,
            [], [], []));
    }

    [Fact]
    public void DetailRejectsOversizeCollectionsAndUnicodeScalarLines()
    {
        var tooMany = Enumerable.Range(
                0, ProtocolConstants.QuestDetailCollectionMaximum + 1)
            .Select(index => new QuestDependency($"quest-{index}", "Title"))
            .ToArray();
        var line = string.Concat(Enumerable.Repeat(
            "🙂", ProtocolConstants.QuestDescriptionLineScalarMaximum + 1));
        var objective = new QuestObjective(
            "task-1", "ftbquests:item", "Craft a shaft",
            1, 1, "1 / 1", true, false);
        var reward = new QuestReward(
            "reward-1", "ftbquests:item", "8 Andesite Alloy",
            false, false, QuestRewardClaimState.Claimable);

        Assert.Throws<ArgumentOutOfRangeException>(() =>
            CreateDetail(dependencies: tooMany));
        Assert.Throws<ArgumentOutOfRangeException>(() =>
            CreateDetail(objectives: Enumerable.Repeat(
                objective, ProtocolConstants.QuestDetailCollectionMaximum + 1).ToArray()));
        Assert.Throws<ArgumentOutOfRangeException>(() =>
            CreateDetail(rewards: Enumerable.Repeat(
                reward, ProtocolConstants.QuestDetailCollectionMaximum + 1).ToArray()));
        Assert.Throws<ArgumentOutOfRangeException>(() =>
            CreateDetail(description: [line]));
    }

    [Fact]
    public void QueryRequiresSearchFilterAndExactTruncationAccounting()
    {
        Assert.Throws<ArgumentNullException>(() =>
            new QuestQueryRequest(QuestQueryScope.Search));
        Assert.Throws<ArgumentException>(() =>
            new QuestQueryRequest(QuestQueryScope.Active, "ignored"));
        Assert.Throws<ArgumentException>(() =>
            new QuestQueryResponse([], 1, false));
    }

    [Fact]
    public void AutoClaimModeRemainsIndependentFromCurrentClaimState()
    {
        var reward = new QuestReward(
            "reward-1", "ftbquests:item", "Reward",
            false, true, QuestRewardClaimState.Claimed);

        Assert.True(reward.AutoClaim);
        Assert.Equal(QuestRewardClaimState.Claimed, reward.ClaimState);
    }

    [Theory]
    [InlineData("quests.query", "{\"scope\":\"active\",\"unknown\":true}")]
    [InlineData("quests.detail", "{\"questId\":\"quest-1\",\"unknown\":true}")]
    public void TypedQuestRequestsRejectUnknownFields(string action, string payload)
    {
        var type = action == "quests.query"
            ? typeof(QuestQueryRequest)
            : typeof(QuestDetailRequest);

        Assert.ThrowsAny<JsonException>(() =>
            JsonSerializer.Deserialize(payload, type, ProtocolJson.Options));
    }

    private static QuestDetailResponse CreateDetail(
        IReadOnlyList<string>? description = null,
        IReadOnlyList<QuestObjective>? objectives = null,
        IReadOnlyList<QuestReward>? rewards = null,
        IReadOnlyList<QuestDependency>? dependencies = null) =>
        new(
            "quest-1", "Getting Started", "Make Power", "Create basics",
            QuestDescriptionVisibility.Visible,
            description ?? ["Build a water wheel.", "Connect it with shafts."],
            QuestState.Active, 0.5, false, false,
            objectives ?? [new QuestObjective(
                "task-1", "ftbquests:item", "Craft a shaft",
                1, 1, "1 / 1", true, false)],
            rewards ?? [new QuestReward(
                "reward-1", "ftbquests:item", "8 Andesite Alloy",
                false, false, QuestRewardClaimState.Claimable)],
            dependencies ?? [new QuestDependency("parent-1", "Welcome")]);
}
