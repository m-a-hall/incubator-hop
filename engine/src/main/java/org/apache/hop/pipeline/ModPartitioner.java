/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.pipeline;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.xml.XMLHandler;
import org.w3c.dom.Node;

public class ModPartitioner extends BasePartitioner {

  private String fieldName;
  protected int partitionColumnIndex = -1;

  public ModPartitioner() {
    super();
  }

  public IPartitioner getInstance() {
    IPartitioner partitioner = new ModPartitioner();
    partitioner.setId( getId() );
    partitioner.setDescription( getDescription() );
    return partitioner;
  }

  public ModPartitioner clone() {
    ModPartitioner modPartitioner = (ModPartitioner) super.clone();
    modPartitioner.fieldName = fieldName;

    return modPartitioner;
  }

  public String getDialogClassName() {
    return "org.apache.hop.ui.pipeline.dialog.ModPartitionerDialog";
  }

  public int getPartition( IRowMeta rowMeta, Object[] row ) throws HopException {
    init( rowMeta );

    if ( partitionColumnIndex < 0 ) {
      partitionColumnIndex = rowMeta.indexOfValue( fieldName );
      if ( partitionColumnIndex < 0 ) {
        throw new HopTransformException( "Unable to find partitioning field name ["
          + fieldName + "] in the output row..." + rowMeta );
      }
    }

    long value;

    IValueMeta valueMeta = rowMeta.getValueMeta( partitionColumnIndex );
    Object valueData = row[ partitionColumnIndex ];

    switch ( valueMeta.getType() ) {
      case IValueMeta.TYPE_INTEGER:
        Long longValue = rowMeta.getInteger( row, partitionColumnIndex );
        if ( longValue == null ) {
          value = valueMeta.hashCode( valueData );
        } else {
          value = longValue.longValue();
        }
        break;
      default:
        value = valueMeta.hashCode( valueData );
    }

    /*
     * value = rowMeta.getInteger(row, partitionColumnIndex);
     */

    int targetLocation = (int) ( Math.abs( value ) % nrPartitions );

    return targetLocation;
  }

  public String getDescription() {
    String description = "Mod partitioner";
    if ( !Utils.isEmpty( fieldName ) ) {
      description += "(" + fieldName + ")";
    }
    return description;
  }

  public String getXML() {
    StringBuilder xml = new StringBuilder( 150 );
    xml.append( "           " ).append( XMLHandler.addTagValue( "field_name", fieldName ) );
    return xml.toString();
  }

  public void loadXML( Node partitioningMethodNode ) throws HopXMLException {
    fieldName = XMLHandler.getTagValue( partitioningMethodNode, "field_name" );
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName( String fieldName ) {
    this.fieldName = fieldName;
  }

}