package org.nervos.neuron.item.NeuronDApp;

/**
 * Created by BaojunCZ on 2018/11/5.
 */
public class TakePhotoItem extends BaseNeuronDAppCallbackItem {

    public Info info;

    public TakePhotoItem(String status, String errorCode, String errorMsg, String imagePhoto) {
        this.status = status;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        info = new Info();
        info.imagePhoto = imagePhoto;
    }

    public class Info {
        public String imagePhoto;
    }
}
