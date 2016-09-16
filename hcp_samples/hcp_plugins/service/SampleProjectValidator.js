define({
    validate : function(oTemplate) {
        var that = this;
        var oTemplateAdditionalData = oTemplate.getAdditionalData();
        if (oTemplateAdditionalData && oTemplateAdditionalData.projectName) {
            var sProjName = oTemplateAdditionalData.projectName;
            return this.context.service.filesystem.documentProvider.getDocument("/" + sProjName).then(function(oPathResult) {
                if (oPathResult) {
                    throw new Error(that.context.i18n.getText("SampleProjectValidator_projectExistsError", [sProjName]));
                }
                else {
                    return true;
                }
            });
        }
        else {
            return true; // If no specific name is defined - validation should pass, default project name shell be decided by wizard
        }
    }
});