package org.nervos.neuron.service.response;

import com.google.gson.annotations.SerializedName;

public class ManifestResponse {

    @SerializedName("chain-id")
    public String chainId;

    @SerializedName("http-provider")
    public String httpProvider;

    @SerializedName("block-viewer")
    public String blockViewer;

}
