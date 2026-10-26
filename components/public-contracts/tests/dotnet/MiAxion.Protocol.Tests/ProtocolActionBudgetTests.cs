using System.Text.Json;
using MiAxion.Protocol.Contracts;
using MiAxion.Protocol.Runtime;

namespace MiAxion.Protocol.Tests;

public sealed class ProtocolActionBudgetTests
{
    [Fact]
    public void PolicyMatchesTheSharedCrossLanguageFixtureAndKeepsControlDeadlines()
    {
        var directory = new DirectoryInfo(AppContext.BaseDirectory);
        while (directory is not null && !File.Exists(Path.Combine(directory.FullName, "tests/fixtures/bridge/action-budget-policy.json"))) directory = directory.Parent;
        Assert.NotNull(directory);
        using var fixture = JsonDocument.Parse(File.ReadAllText(Path.Combine(directory.FullName, "tests/fixtures/bridge/action-budget-policy.json")));
        var queries = fixture.RootElement.GetProperty("queryActions").EnumerateArray().Select(v => v.GetString()).ToHashSet(StringComparer.Ordinal);
        var recipes = fixture.RootElement.GetProperty("recipeActions").EnumerateArray().Select(v => v.GetString()).ToHashSet(StringComparer.Ordinal);
        Assert.Equal(ProtocolActionBudget.Capability, fixture.RootElement.GetProperty("capability").GetString());
        var queryActions = queries.Select(Parse).ToHashSet();
        var recipeActions = recipes.Select(Parse).ToHashSet();
        var limits = ProtocolRuntimeLimitsLoader.Load();
        foreach (var action in Enum.GetValues<KnownProtocolAction>())
        {
            Assert.Equal(queryActions.Contains(action), ProtocolActionBudget.IsQuery(action));
            Assert.Equal(recipeActions.Contains(action), ProtocolActionBudget.IsRecipe(action));
        }
        foreach (var wire in queries)
            Assert.Equal(limits.QueryDeadlineMaximumMs, ProtocolActionBudget.MaximumDeadlineMs(new ProtocolAction(wire!), limits));

        static KnownProtocolAction Parse(string? wire)
        {
            Assert.True(new ProtocolAction(wire!).TryGetKnown(out var action));
            return action;
        }
    }
}
