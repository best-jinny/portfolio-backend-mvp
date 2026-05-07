package com.hyejin.portfolio.evidence.application.service;

import com.hyejin.portfolio.evidence.application.port.in.CreateEvidenceLinksUseCase;
import com.hyejin.portfolio.evidence.domain.EvidenceLink;

import java.util.UUID;

public class CreateEvidenceLinksService implements CreateEvidenceLinksUseCase {
    @Override
    public Result create(Command command) {
        var links = command.targets().stream()
            .map(target -> new EvidenceLink(
                UUID.randomUUID(),
                command.claim().id(),
                target.targetModule(),
                target.targetType(),
                target.targetId(),
                target.targetAnchor(),
                target.relationType()
            ))
            .toList();
        return new Result(command.claim().id(), links);
    }
}
