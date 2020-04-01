<?php

class TestsController extends YuppController {

   public function indexAction()
   {
      return $this->renderString("Bienvenido a su nueva aplicacion!");
   }

   public function listPatientsAction()
   {
      $file = "./apps/xre-client/resources/demographic_list.xml";
	  $xml = FileSystem::read($file);
	  //header('Content-type: text/xml');
      //return $this->renderString( $xml );
	  
	  /*
	  <persons>
		<person>
			<id>abc123</id>
			<name>
				<given>Pablo</given>
				<given>Federico</given>
				<lastname>Pazos</lastname>
				<lastname>Gutierrez</lastname>
			</name>
			<birthdate>19811024</birthdate>
			<sex>
				<name>Male</name>
				<code>M</code>
			</sex>
		</person>
		<person>
			<id>abc234</id>
			<name>
				<given>Carla</given>
				<given>Cristina</given>
				<lastname>Lopez</lastname>
				<lastname>Santos</lastname>
			</name>
			<birthdate>19690325</birthdate>
			<sex>
				<name>Female</name>
				<code>F</code>
			</sex>
		</person>
		<person>
			<id>abc345</id>
			<name>
				<given>Ana</given>
				<given>Camila</given>
				<lastname>Rodriguez</lastname>
				<lastname>Zas</lastname>
			</name>
			<birthdate>19930621</birthdate>
			<sex>
				<name>Female</name>
				<code>F</code>
			</sex>
			</person>
		</persons>
	  */
	  
	  //$xml_obj = new SimpleXMLElement($xml);
	  //$sessionIdPath = $xml_obj->xpath("/map/entry[@key='sessionId']");
	  //$sessionId = (string)$sessionIdPath[0];\
	  
	  $personas = $this->simpleXMLToArray(new SimpleXMLElement($xml));
	  /*
	  Array
		(
			[person] => Array
				(
					[0] => Array
						(
							[id] => abc123
							[name] => Array
								(
									[given] => Array
										(
											[0] => Pablo
											[1] => Federico
										)

									[lastname] => Array
										(
											[0] => Pazos
											[1] => Gutierrez
										)

								)

							[birthdate] => 19811024
							[sex] => Array
								(
									[name] => Male
									[code] => M
								)

						)

					[1] => Array
						(
							[id] => abc234
							[name] => Array
								(
									[given] => Array
										(
											[0] => Carla
											[1] => Cristina
										)

									[lastname] => Array
										(
											[0] => Lopez
											[1] => Santos
										)

								)

							[birthdate] => 19690325
							[sex] => Array
								(
									[name] => Female
									[code] => F
								)

						)

					[2] => Array
						(
							[id] => abc345
							[name] => Array
								(
									[given] => Array
										(
											[0] => Ana
											[1] => Camila
										)

									[lastname] => Array
										(
											[0] => Rodriguez
											[1] => Zas
										)

								)

							[birthdate] => 19930621
							[sex] => Array
								(
									[name] => Female
									[code] => F
								)

						)

				)

		)
	  */
	  return array('personas' => $personas['person']);
	  
	  //return $this->renderString( print_r( $this->simpleXMLToArray(new SimpleXMLElement($xml)), true ) );
   }
   
   
   
   public function listPatientsXMLAction()
   {
      $file = "./apps/xre-client/resources/demographic_list.xml";
	  $xml = FileSystem::read($file);
	  header('Content-type: text/xml');
      return $this->renderString( $xml );
   }
   
   public function getPatientAction()
   {
      $pid = $this->params['patient_id'];
      $file = "./apps/xre-client/resources/demographic_".$pid.".xml";
	  $xml = FileSystem::read($file);
	  header('Content-type: text/xml');
      return $this->renderString( $xml );
   }
   
   public function papRuleAction()
   {
      $pid = $this->params['patient_id'];
      //$file = "./apps/xre-client/resources/demographic_".$pid.".xml";
	  //$xml = FileSystem::read($file);
	  
	  YuppLoader::load('core.http','HTTPRequest');
	  
	  /*
	  <map>
		<entry key="unit_rule_date_diff.v1">
		<entry key="id">rule_date_diff.v1</entry>
		<entry key="name">
		regla de prueba para calculos de diferencias entre fechas
		</entry>
		<entry key="description"/>
		</entry>
		<entry key="rule4.v1">
		<entry key="id">rule_4_1.v1</entry>
		<entry key="name">Regla con return de valores</entry>
		<entry key="description"/>
		</entry>
		<entry key="rule5.v1">
		<entry key="id">rule_5_1.v1</entry>
		<entry key="name">Regla con return de valores</entry>
		<entry key="description"/>
		</entry>
		<entry key="mod.rule6.v1">
		<entry key="id">rule_6_1.v1</entry>
		<entry key="name">Regla con return de agregaciones</entry>
		<entry key="description"/>
		</entry>
		<entry key="mod.rule7_error_url.v1">
		<entry key="id">rule7_error_url.v1</entry>
		<entry key="name">Regla con errores</entry>
		<entry key="description"/>
		</entry>
      </map>
	  $url = "http://localhost:8080/rule-engine-ui/rest/list?patient_id=$pid&format=xml";
	  $req = new HTTPRequest();
      //$res = $req->HttpRequestPost( $url );
      $res = $req->HttpRequestGet( $url );
	  $xml = $res->getBody();
	  */
	  
	  // add ------
	  $unitId = 'unit_rule_date_diff.v1';
	  $ruleId = 'rule_date_diff.v1';
	  $url = "http://localhost:8080/rule-engine-ui/rest/add?unitId=$unitId&ruleId=$ruleId&format=xml";
	  $req = new HTTPRequest();
      $res = $req->HttpRequestGet( $url );
	  $xml = $res->getBody();
	  
	  /*
	  <map>
    	<entry key="sessionId">9082841c-c5c7-4c2c-93e6-bb86f60e5ccc</entry>
	    <entry key="msg">rule added to executer</entry>
	  </map>
	  */
	  
	  // parse add response ------
	  $xml_obj = new SimpleXMLElement($xml);
	  $sessionIdPath = $xml_obj->xpath("/map/entry[@key='sessionId']");
	  $sessionId = (string)$sessionIdPath[0]; // 9082841c-c5c7-4c2c-93e6-bb86f60e5ccc
	  
	  // init ------
	  $url = "http://localhost:8080/rule-engine-ui/rest/init?patient_id=$pid&sessionId=$sessionId&format=xml";
	  $req = new HTTPRequest();
      $res = $req->HttpRequestGet( $url );
	  $xml = $res->getBody();
	  
	  /*
	  <map>
	    <entry key="sessionId">a56821f9-cd62-4cd4-aae5-0281be5c38cd</entry>
		<entry key="msg">rule initialization error</entry>
	  </map>
	  
	  <map>
	    <entry key="sessionId">abb59eb8-3621-478c-81ac-902703a0ec17</entry>
		<entry key="msg">rule initialized</entry>
      </map>
	  */
	  
	  // exec ------
	  $url = "http://localhost:8080/rule-engine-ui/rest/exec?sessionId=$sessionId&format=xml";
	  $req = new HTTPRequest();
      $res = $req->HttpRequestGet( $url );
	  $xml = $res->getBody();
	  
	  /*
	  <map>
	    <entry key="sessionId">a9defd8c-f506-4fc2-99a1-4c5186ec1d3a</entry>
		<entry key="msg">Ejecucion exitosa</entry>
	  </map>
	  */
	  
	  // result ------
	  $url = "http://localhost:8080/rule-engine-ui/rest/result?sessionId=$sessionId&format=json";
	  $req = new HTTPRequest();
      $res = $req->HttpRequestGet( $url );
	  $xml = $res->getBody();
	  
	  /*
	  <map>
	    <entry key="sessionId">8c508d4e-a186-4fe8-9a16-c9f68fbf1573</entry>
		<entry key="msg">Rule 8c508d4e-a186-4fe8-9a16-c9f68fbf1573 removed from executor</entry>
		<entry key="result">
		  <entry key="last_pap_test_date">2011-03-17 00:00:00.0 GFT</entry>
		  <entry key="days_from_last_pap">551</entry>
		  <entry key="ret_warning">warning</entry>
		</entry>
	  </map>
	  
	  {
		"sessionId": "ba14722b-7432-4e6d-99e2-c6bd4ab8caa1",
		"msg": "Rule ba14722b-7432-4e6d-99e2-c6bd4ab8caa1 removed from executor",
		"result": {
			"last_pap_test_date": "2011-03-17T03:00:00Z",
			"days_from_last_pap": 551,
			"ret_warning": "warning"
		}
	  }
	  */
	  
	  
	  //header('Content-type: text/xml');
	  header('Content-type: application/json');
      return $this->renderString( $xml );
   }
   
   
   public function papResultsAction()
   {
      $pid = $this->params['patient_id'];
      $file = "./apps/xre-client/resources/gine_tests_".$pid.".xml";
	  $xml = FileSystem::read($file);
	  header('Content-type: text/xml');
      return $this->renderString( $xml );
   }
   
   
   /** 
	 * Converts a simpleXML element into an array. Preserves attributes.<br/> 
	 * You can choose to get your elements either flattened, or stored in a custom 
	 * index that you define.<br/> 
	 * For example, for a given element 
	 * <code> 
	 * <field name="someName" type="someType"/> 
	 * </code> 
	 * <br> 
	 * if you choose to flatten attributes, you would get: 
	 * <code> 
	 * $array['field']['name'] = 'someName'; 
	 * $array['field']['type'] = 'someType'; 
	 * </code> 
	 * If you choose not to flatten, you get: 
	 * <code> 
	 * $array['field']['@attributes']['name'] = 'someName'; 
	 * </code> 
	 * <br>__________________________________________________________<br> 
	 * Repeating fields are stored in indexed arrays. so for a markup such as: 
	 * <code> 
	 * <parent> 
	 *     <child>a</child> 
	 *     <child>b</child> 
	 *     <child>c</child> 
	 * ... 
	 * </code> 
	 * you array would be: 
	 * <code> 
	 * $array['parent']['child'][0] = 'a'; 
	 * $array['parent']['child'][1] = 'b'; 
	 * ...And so on. 
	 * </code> 
	 * @param simpleXMLElement    $xml            the XML to convert 
	 * @param boolean|string    $attributesKey    if you pass TRUE, all values will be 
	 *                                            stored under an '@attributes' index. 
	 *                                            Note that you can also pass a string 
	 *                                            to change the default index.<br/> 
	 *                                            defaults to null. 
	 * @param boolean|string    $childrenKey    if you pass TRUE, all values will be 
	 *                                            stored under an '@children' index. 
	 *                                            Note that you can also pass a string 
	 *                                            to change the default index.<br/> 
	 *                                            defaults to null. 
	 * @param boolean|string    $valueKey        if you pass TRUE, all values will be 
	 *                                            stored under an '@values' index. Note 
	 *                                            that you can also pass a string to 
	 *                                            change the default index.<br/> 
	 *                                            defaults to null. 
	 * @return array the resulting array. 
	 */ 
	private function simpleXMLToArray(SimpleXMLElement $xml,$attributesKey=null,$childrenKey=null,$valueKey=null){ 

		if($childrenKey && !is_string($childrenKey)){$childrenKey = '@children';} 
		if($attributesKey && !is_string($attributesKey)){$attributesKey = '@attributes';} 
		if($valueKey && !is_string($valueKey)){$valueKey = '@values';} 

		$return = array(); 
		$name = $xml->getName(); 
		$_value = trim((string)$xml); 
		if(!strlen($_value)){$_value = null;}; 

		if($_value!==null){ 
			if($valueKey){$return[$valueKey] = $_value;} 
			else{$return = $_value;} 
		} 

		$children = array(); 
		$first = true; 
		foreach($xml->children() as $elementName => $child){ 
			$value = $this->simpleXMLToArray($child,$attributesKey, $childrenKey,$valueKey); 
			if(isset($children[$elementName])){ 
				if(is_array($children[$elementName])){ 
					if($first){ 
						$temp = $children[$elementName]; 
						unset($children[$elementName]); 
						$children[$elementName][] = $temp; 
						$first=false; 
					} 
					$children[$elementName][] = $value; 
				}else{ 
					$children[$elementName] = array($children[$elementName],$value); 
				} 
			} 
			else{ 
				$children[$elementName] = $value; 
			} 
		} 
		if($children){ 
			if($childrenKey){$return[$childrenKey] = $children;} 
			else{$return = array_merge($return,$children);} 
		} 

		$attributes = array(); 
		foreach($xml->attributes() as $name=>$value){ 
			$attributes[$name] = trim($value); 
		} 
		if($attributes){ 
			if($attributesKey){$return[$attributesKey] = $attributes;} 
			else{$return = array_merge($return, $attributes);} 
		} 

		return $return; 
	} 
}

?>