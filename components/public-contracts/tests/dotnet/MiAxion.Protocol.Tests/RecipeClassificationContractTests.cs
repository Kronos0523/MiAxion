using MiAxion.Protocol.Contracts;

namespace MiAxion.Protocol.Tests;

public sealed class RecipeClassificationContractTests
{
    [Theory]
    [InlineData("crafting", "deterministic")]
    [InlineData("machine_processing", "deterministic")]
    [InlineData("drops", "acquisition_only")]
    [InlineData("loot", "acquisition_only")]
    [InlineData("conversion", "deterministic")]
    [InlineData("other_unclassified", "insufficient_data")]
    public void AcceptsClosedClassificationContract(string category, string eligibility)
    {
        var value = new RecipeClassificationDto(category, eligibility, "Source category");

        Assert.Equal(category, value.Category);
        Assert.Equal(eligibility, value.PlanningEligibility);
        Assert.Equal("Source category", value.VisibleCategoryName);
    }

    [Theory]
    [InlineData("drops", "deterministic")]
    [InlineData("loot", "deterministic")]
    [InlineData("other_unclassified", "deterministic")]
    [InlineData("future_category", "acquisition_only")]
    [InlineData("machine_processing", "future_eligibility")]
    public void RejectsUnknownOrUnsafeClassificationValues(string category, string eligibility)
    {
        Assert.Throws<ArgumentException>(() => new RecipeClassificationDto(category, eligibility));
    }

    [Fact]
    public void VisibleCategoryNameIsBoundedAndRejectsControlText()
    {
        Assert.Throws<ArgumentException>(() => new RecipeClassificationDto(
            "crafting", "deterministic", new string('x', 257)));
        Assert.Throws<ArgumentException>(() => new RecipeClassificationDto(
            "crafting", "deterministic", "name\u0085break"));
    }
}
