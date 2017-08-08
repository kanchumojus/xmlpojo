package com

import scala.collection.mutable.Set


class TagInfo(nodeName:String) {
  
  var name:String=nodeName;
  
  /**
   * Defined tags for holding the node elements based on the structure of the XML node Structure
   * 1. simpleTags is for holding the Node elements which only have text to them
   * (eg. <DomainName>gpshospitality.com</DomainName>)
   * 2. complexTags is for holding the Node elements which can have children to them, but this children have some data to them
   * eg.(<Total><TotalType>Credit</TotalType><TotalAmount>1172.8900</TotalAmount></Total>) here <Total> becomes the complex tag.
   * 3. emptyChildTags is for holding the Node elements which can have children to them, but this children doesn't have any data in them
   * eg.<CustomerInfo><CustomerLastName/><CustomerAccountNumber/></CustomerInfo>  here <CustomerInfo> becomes the emptyChildTags tag
   */
  var simpleTag = Set[String]();
  var complexTag = Set[String]();
  var maxCounts:Int=0;
  var attributes=Set[String]();
  
  
}