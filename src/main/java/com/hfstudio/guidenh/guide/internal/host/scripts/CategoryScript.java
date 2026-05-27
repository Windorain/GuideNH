package com.hfstudio.guidenh.guide.internal.host.scripts;

import java.util.List;

import com.hfstudio.guidenh.guide.PageAnchor;
import com.hfstudio.guidenh.guide.compiler.tags.mediawiki.CategoryCompiler.CategoryPlaceholder;
import com.hfstudio.guidenh.guide.document.block.LytParagraph;
import com.hfstudio.guidenh.guide.document.block.LytVBox;
import com.hfstudio.guidenh.guide.document.flow.LytFlowLink;
import com.hfstudio.guidenh.guide.indices.CategoryIndex;
import com.hfstudio.guidenh.guide.internal.host.EventType;
import com.hfstudio.guidenh.guide.internal.host.LytEvent;
import com.hfstudio.guidenh.guide.internal.host.LytScript;
import com.hfstudio.guidenh.guide.internal.host.ScriptContext;
import com.hfstudio.guidenh.guide.internal.host.ScriptType;

public class CategoryScript implements LytScript {

    @Override
    public ScriptType type() { return ScriptType.JAVA; }

    @Override
    public String styleClass() { return "Category"; }

    @Override
    public void onEvent(Object node, LytEvent event, ScriptContext ctx) {
        if (event.type() != EventType.MOUNT) return;
        if (!(node instanceof CategoryPlaceholder ph)) return;

        CategoryIndex index = ctx.getIndex(CategoryIndex.class);
        if (index == null) return;

        List<PageAnchor> members = index.get(ph.name);
        LytVBox box = new LytVBox();
        int count = 0;
        for (PageAnchor anchor : members) {
            if (ph.rows > 0 && count >= ph.rows) break;
            LytParagraph line = new LytParagraph();
            LytFlowLink link = new LytFlowLink();
            link.setGuideLink(ph.guideId, anchor);
            link.appendText(anchor.pageId().getResourcePath());
            line.append(link);
            box.append(line);
            count++;
        }
        ctx.replace(box);
    }
}
