package com.hyejin.portfolio.worker.application;

import java.util.List;
import java.util.UUID;

public record MockProposalJobResult(UUID intentId, List<String> steps) {
}
