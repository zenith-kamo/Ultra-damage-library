package com.zenith.udl.renderblender.api.iface;

import com.zenith.udl.renderblender.api.client.util.TransformUtils;
import com.zenith.udl.renderblender.api.client.model.PerspectiveModelState;

public interface IToolTransform {
    
    default PerspectiveModelState getToolTransform() {
        return TransformUtils.DEFAULT_TOOL;
    }

}
