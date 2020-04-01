<?php

global $_base_dir;

$m = Model::getInstance();
$personas = $m->get('personas');

YuppLoader::load('core.mvc', 'DisplayHelper');

?>
<html>
  <head>
    <META HTTP-EQUIV="CONTENT-TYPE" CONTENT="text/html; charset=UTF-8">
    <style>
      body {
        font-family: arial;
        font-size: 12px;
      }
      th, td {
        font-family: arial;
        font-size: 12px;
      }
      td ul {
        list-style: none;
        padding: 0px;
        margin: 0px;
      }
      td ul li {
        display: block;
      }
      
      /* este estilo es para ul como un menu de botones, no como tabs */
      ul.actions {
        list-style: none;
        display: block;
        padding: 0px;
      }
      ul.actions li {
        display: inline;
        cursor: pointer;
      }
      ul.actions li a {
        text-decoration: none;
        color: #999;
        background-color: #ffff80;
        padding: 4px 10px 5px 10px;
      }
      ul.actions li a:hover {
        color: #000000;
        background-color: #ffee00;
      }
      ul.actions li.selected {
        color: #000;
      }
      
      .order_desc, .order_asc {
         background-position: 0px;
         background-repeat: no-repeat;
         padding-left: 12px;
      }
      .order_asc {
         background-image: url(<?php echo $_base_dir; ?>/images/order_asc.gif);
      }
      .order_desc {
         background-image: url(<?php echo $_base_dir; ?>/images/order_desc.gif);
      }
      
      .controls {
         display: block;
         padding-top: 10px;
         padding-bottom: 10px;
      }
      
      form {
        display: inline;
        padding: 10px;
        background-color: #eee;
      }
      select {
        width: 140px;
      }
      select[name="max"] {
        width: 50px;
      }
      .field_container {
         display: inline;
      }
      .field_container .field {
         display: inline;
      }
      .field_container .label, .label {
         display: inline;
         padding-right: 5px;
      }
	  
	  .description {
	    padding: 10px;
		color: #000;
		background-color: #efefef;
		-webkit-border-radius: 5px;
		-moz-border-radius: 5px;
		border-radius: 5px;
	  }
	  .feedback {
	    padding: 5px;
		color: #fff;
		-webkit-border-radius: 5px;
		-moz-border-radius: 5px;
		border-radius: 5px;
	  }
	  .warning {
		background-color: #DA4F49;
		background-image: -webkit-linear-gradient(top, #EE5F5B, #BD362F);
		background-position: 0% 0%;
		background-repeat: repeat-x;
		border-bottom-color: rgba(0, 0, 0, 0.246094);
      }
	  .ok {
	    background-color: #51A351;
		background-image: -webkit-linear-gradient(top, #62C462, #51A351);
		background-position: 0px -15px;
		background-repeat: repeat-x;
		border-bottom-color: rgba(0, 0, 0, 0.246094);
	  }
	  .not_applicable {
	    background-color: #2F96B4;
		background-image: -webkit-linear-gradient(top, #5BC0DE, #2F96B4);
		background-position: 0px -15px;
		background-repeat: repeat-x;
		border-bottom-color: rgba(0, 0, 0, 0.246094);
	  }
	  
    </style>
	<?php h('js', array('name'=>'jquery-1.8.0.min', 'app'=>'xre-client')); ?>
	<script>
	  var papRule = function(patientId) {
    
		var jqxhr = $.get(
			"http://localhost/YuppPHPFramework/xre-client/tests/papRule",
			{
			  "patient_id": patientId
			},
			function(obj, status, response) { // handler de success

			  /*
			  {"sessionId":"9a72211f-f6f8-41ac-a694-fd82973c6e33",
			   "msg":"Rule 9a72211f-f6f8-41ac-a694-fd82973c6e33 removed from executor",
			   "result":{"last_pap_test_date":"2002-02-19T03:00:00Z",
			             "days_from_last_pap":3865,
						 "code":"warning"}}
			  */
			  if (obj.result.code == undefined)
			  {
			    //alert('no tiene result');
				//$('#alert_'+patientId).html( JSON.stringify(obj) );
				$('#alert_'+patientId).html('Not applicable');
				$('#alert_'+patientId).addClass('not_applicable');
			  }
			  else
			  {
				//alert(obj.result.code); // ok o warning
				//$('#alert_'+patientId).html( JSON.stringify(obj) );
				
				var message;
				if (obj.result.days_from_last_pap < 365)
				{
				  message = 'Last PAP '+ (obj.result.days_from_last_pap / 30).toFixed(1) + ' months ago';
				}
				else
				{
				  message = 'Last PAP '+ (obj.result.days_from_last_pap / 365).toFixed(1) + ' years ago';
				}
				
				$('#alert_'+patientId).html(message);
				
				if (obj.result.code == 'ok')
				{
				  $('#alert_'+patientId).addClass('ok');
				}
				else (obj.result.code == 'warning')
				{
				  $('#alert_'+patientId).addClass('warning');
				}
			  }
			  
			  //$('#alert_'+patientId).html( JSON.stringify(obj) );
			  
			  console.log( JSON.stringify(obj) );
			},
			'json'
		)
		.error(
    	  function(a,b,c) {
    		log("error");
    		log(JSON.stringify(a));
    		log(JSON.stringify(b));
    		log(JSON.stringify(c));
    	  }
    	);
      };
	</script>
  </head>
  <body>
    <h1>Patient list: PAP test check</h1>
	<div class="description">
	  <b>Rule: every woman should should have a Papanicolau Test once a year</b><br/>
	  This demo test that rule, marking male patients as "not applicable",
	  and sending a warning to the clinician if the female patient last PAP
	  test was made more than a year ago.
	</div>
      
    <?php if ($m->flash('message')) : ?>
      <div class="flash"><?php echo $m->flash('message'); ?></div><br/>
    <?php endif; ?>
    
    
    <ul class="actions">
      <li><?php /*echo h('link', array('controller' => 'conteos',
                                     'action' => 'index',
                                    'body' => 'Conteos') );*/ ?></li>
    </ul>
    <table border="1" cellpadding="5" cellspacing="0">
        <tr>
          <th>id</th>
          <th>primer nombre</th>
          <th>segundo nombre</th>
          <th>primer apellido</th>
          <th>segundo apellido</th>
          <th>fecha de nacimiento</th>
          <th>sexo</th>
		  <th>alertas</th>
        </tr>
        <?php foreach ( $personas as $persona ) : ?>
          <tr>
            <td><?php echo $persona['id']; ?></td>
            <td><?php echo $persona['name']['given'][0]; ?></td>
            <td><?php echo $persona['name']['given'][1]; ?></td>
            <td><?php echo $persona['name']['lastname'][0]; ?></td>
            <td><?php echo $persona['name']['lastname'][1]; ?></td>
            <td><?php echo $persona['birthdate']; ?></td>
			<td><?php echo $persona['sex']['code']; ?></td>
			<td>
			  <div id="alert_<?php echo $persona['id']; ?>" class="feedback"></div>
			  <script>
			    papRule('<?php echo $persona['id']; ?>');
			  </script>
			</td>
          </tr>
        <?php endforeach; ?>
    </table>
  </body>
</html>