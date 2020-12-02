$(function() {
    $('#form').submit(function(){
        $("#loader").removeClass("hidden").addClass("cursor-not-allowed").prop("disabled", true);
        $.ajax({
            url: $('#form').attr('action'),
            type: 'POST',
            data : $('#form').serialize(),
            xhrFields: {
                responseType: 'blob'
            },
            success: function(resp){
                const url = window.URL.createObjectURL(resp);
                const a = document.createElement('a');
                a.style.display = 'none';
                a.href = url;
                // the filename you want
                a.download = 'severell.zip';
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                $("#loader").addClass("hidden").removeClass("cursor-not-allowed").prop("disabled", false);
                $('#form').find("input[type=text], textarea").val("");
            }
        });
        return false;
    });
});