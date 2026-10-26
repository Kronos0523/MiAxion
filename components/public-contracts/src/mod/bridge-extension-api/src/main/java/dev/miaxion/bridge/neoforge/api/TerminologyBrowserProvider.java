package dev.miaxion.bridge.neoforge.api;

import java.util.Objects;

/** Client-thread-only, targeted current-hover read. It has no transport or consent authority. */
public interface TerminologyBrowserProvider {
    enum Status { READY, NOT_INSTALLED, INCOMPATIBLE, NOT_IMPLEMENTED, RUNTIME_UNAVAILABLE }
    enum Kind { ITEM, BLOCK, ENTITY, FLUID, CHEMICAL, ENERGY, CUSTOM }
    record Support(Status hover, Status inspectionEvents, boolean productionConnected) {
        public Support { Objects.requireNonNull(hover); Objects.requireNonNull(inspectionEvents); }
    }
    record Identity(Kind kind, String resourceId, String ingredientTypeUid) {
        public Identity { Objects.requireNonNull(kind); }
    }
    record Target(Identity identity, String displayName, String finalTooltipTitle, ObservedItemDetails itemDetails) {
        public Target(Identity identity, String displayName, String finalTooltipTitle) { this(identity, displayName, finalTooltipTitle, null); }
        public Target { if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("Display name is required"); }
    }
    record Focus(String visitKey, Target target) {
        public Focus { Objects.requireNonNull(visitKey); Objects.requireNonNull(target); }
    }
    String providerId();
    Support support();
    Focus hover();
}
