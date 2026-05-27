package com.hfstudio.guidenh.guide.internal.host;

import java.util.HashMap;
import java.util.Map;
import com.hfstudio.guidenh.guide.document.block.LytDocument;
import com.hfstudio.guidenh.guide.document.block.LytNode;

class ScriptContextImpl implements ScriptContext {
    private final Map<String, Object> data = new HashMap<>();
    private final Object node;
    private final LytHost host;
    private final LytDocument document;

    ScriptContextImpl(Object node, LytHost host, LytDocument document) {
        this.node = node;
        this.host = host;
        this.document = document;
    }

    @Override
    public Map<String, Object> data() { return data; }

    @Override
    public void replace(Object newNode) {
        if (node instanceof LytNode ln && newNode instanceof LytNode newLn) {
            LytNode parent = ln.getParent();
            if (parent != null) {
                parent.replaceChild(ln, newLn);
            }
        }
        // Flow-content replacement deferred to Phase 4
    }

    @Override
    public String allocateId(String prefix) {
        return host.allocateNodeUid(host.currentPageId, prefix);
    }

    @Override
    public LytDocument document() { return document; }
}
