define({

    configWizardSteps : function() {
        return [];
    },

    onBeforeTemplateGenerate : function(templateZip, model) {
        model.hostAddr = window.location.protocol + "//" + window.location.host;
        return [ templateZip, model ];
    },

    onAfterGenerate : function(projectZip, model) {

    },

    customValidation : function(model) {
    	// TODO: Add validation on this wizard, that there is no project name conflict.
        //return this.context.service.sampleprojectvalidator.validate(model.selectedTemplate);
    }
});