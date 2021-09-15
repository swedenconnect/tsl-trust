$(document).ready(function () {
    $("#uploadedFileInput").fileinput({
        uploadUrl: "main", // server upload action
        uploadAsync: false,
        showPreview: false,
        allowedFileExtensions: ['xml',"pdf"],
        maxFileSize: maxFileSizeKb,
        maxFileCount: 1,
        language: lang,
        elErrorContainer: '#kv-error-2'
    }).on('filebatchpreupload', function (event, data, id, index) {
        $('#kv-success-2').html('<h4>Upload Status</h4><ul></ul>').hide();
    }).on('filebatchuploadsuccess', function (event, data) {
        setTimeout(function(){window.location="result";},500);
    });

});



