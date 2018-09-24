	// remove
	function remove(group){
		alert(group);
		ComConfirm.show("确认删除应用?", function(){
			$.ajax({
				type : 'POST',
				url : base_url + '/group/remove',
				data : {"namespace":$('#namespace').val(), "group":group},
				dataType : "json",
				success : function(data){
					if (data.code == 200) {
						//获取内容
						var array = data.content;
						
						html = "";
						for(var i=0;i<array.length;i++){
							html = html +
							'<tr>' +
								'<td>'+array[i]+'</td>' +
								'<td>' + 
								'<button type="button" onclick="remove(' + array[i] + ')" >删除</button> ' +
								'</td>' +
							"</tr>"
						}
						
						$("#groupbody").append(html);
					} else {
						if (data.msg) {
							ComAlert.show(2, data.msg);
						} else {
							ComAlert.show(2, '删除失败');
						}
					}
				},
			});
		});
	}

$(function() {

	$("#namespace").change(function(){
		$.ajax({
			type : 'POST',
			url : base_url + '/group/findList',
			data : {"namespace":$('#namespace').val()},
			success : function(data){
				//获取内容
				var array = data.content;
				
				html = "";
				for(var i=0;i<array.length;i++){
					html = html +
					'<tr>' +
						'<td>'+array[i]+'</td>' +
						'<td>' + 
						'<button type="button" onclick="remove(' + array[i] + ')" >删除</button> ' +
						'</td>' +
					"</tr>"
				}
				
				$("#groupbody").append(html);

			}
		});
	});


	


	// jquery.validate 自定义校验 “英文字母开头，只含有英文字母、数字和下划线”
	jQuery.validator.addMethod("myValid01", function(value, element) {
		//var length = value.length;
		//var valid = /^[a-zA-Z][a-zA-Z0-9-]*$/;
		//return this.optional(element) || valid.test(value);
		return true;
	}, "限制以字母开头，由字母、数字和中划线组成");

	$('.add').on('click', function(){
		$("#addModal .form input[name='namespace']").val( $('#namespace').val() );
		$('#addModal').modal({backdrop: false, keyboard: false}).modal('show');
	});
	var addModalValidate = $("#addModal .form").validate({
		errorElement : 'span',
		errorClass : 'help-block',
		focusInvalid : true,
		rules : {
			group : {
				required : true,
				rangelength:[1,100],
				myValid01 : true
			}
		},
		messages : {
			group : {
				required :"请输入名称",
				rangelength:"长度限制为1~100",
				myValid01: "限制以字母开头，由字母、数字和中划线组成"
			}
		},
		highlight : function(element) {
			$(element).closest('.form-group').addClass('has-error');
		},
		success : function(label) {
			label.closest('.form-group').removeClass('has-error');
			label.remove();
		},
		errorPlacement : function(error, element) {
			element.parent('div').append(error);
		},
		submitHandler : function(form) {
			$.post(base_url + "/group/save",  $("#addModal .form").serialize(), function(data, status) {
				if (data.code == "200") {
					$('#addModal').modal('hide');
					setTimeout(function () {
						//获取内容
						var array = data.content;
						
						html = "";
						for(var i=0;i<array.length;i++){
							html = html +
							'<tr>' +
								'<td>'+array[i]+'</td>' +
								'<td>' + 
								'<button type="button" onclick="remove(' + array[i] + ')" >删除</button> ' +
								'</td>' +
							"</tr>"
						}
						
						$("#groupbody").append(html);
					}, 315);
				} else {
					if (data.msg) {
						ComAlert.show(2, data.msg);
					} else {
						ComAlert.show(2, "新增失败");
					}
				}
			});
		}
	});
	$("#addModal").on('hide.bs.modal', function () {
		$("#addModal .form")[0].reset();
		addModalValidate.resetForm();
		$("#addModal .form .form-group").removeClass("has-error");
	});

});
