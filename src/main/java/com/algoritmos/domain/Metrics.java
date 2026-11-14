package com.algoritmos.domain;

// Conta as comparações e movimentações feitas pelos algoritmos.
public final class Metrics {
    private long comparisons;
    private long movements;

    public void comparacao() { comparisons++; }
    public void movement() { movements++; }
    public long comparisons() { return comparisons; }
    public long movements() { return movements; }
    public void reset() { comparisons = 0; movements = 0; }
}
