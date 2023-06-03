package ca.uhn.example.converter;

import org.apache.ibatis.session.SqlSession;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointSystem;
import org.hl7.fhir.r4.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Patient.ContactComponent;

import ca.uhn.example.common.Common;
import ca.uhn.example.mybatis.MyPatient;

public class MyPatientConverter {
	static public Patient CreatePatientResource(SqlSession session, MyPatient table) {
		try {
			Patient resource = new Patient();

			// meta
			Meta meta = new Meta();
			meta.addProfile("http://jpfhir.jp/fhir/core/StructureDefinition/JP_Patient");
//			meta.setLastUpdatedElement(FhirCommon.CreateInstant(tkanja.getLAST_DATE(), tkanja.getLAST_TIME()));
			resource.setMeta(meta);

			//    id
			resource.setId(table.getPt_id());

			//	identifier
			resource.addIdentifier()
				.setValue(table.getPt_id())
		      	.setSystem(Common.GetNSPatient());

			//	active
			resource.setActive(true);

			//	name
			resource.addName(Common.CreateHumanName(table.getPt_family_ide(), table.getPt_given_ide(), "IDE"));
			resource.addName(Common.CreateHumanName(table.getPt_family_syl(), table.getPt_given_syl(), "SYL"));
			resource.addName(Common.CreateHumanName(table.getPt_family_abc(), table.getPt_given_abc(), "ABC"));

			//	telecom
			if (!table.getPt_phone().isEmpty()) {
				resource.addTelecom()
			      	.setUse(ContactPointUse.HOME)
			      	.setSystem(ContactPointSystem.PHONE)
			      	.setValue(table.getPt_phone());
			}

			//	gender
			switch(table.getPt_gender()) {
			case "M" : resource.setGender(AdministrativeGender.MALE); break;
			case "F" : resource.setGender(AdministrativeGender.FEMALE); break;
			case "O" : resource.setGender(AdministrativeGender.OTHER); break;
			default: resource.setGender(AdministrativeGender.UNKNOWN); break;
			}

			//	birthDate
			resource.setBirthDateElement(new DateType(table.getPt_birthdate()));

			//	deceased[x]
			//		deceasedBoolean
			//		deceasedDateTime
			//	address
			if (! table.getPt_address().isEmpty() || ! table.getPt_zip().isEmpty()) {
				resource.addAddress()
					.setText(table.getPt_address())
					.setCountry("JP")
					.setPostalCode(table.getPt_zip());
		      }

			//	maritalStatus
			//	multipleBirth[x]
			//		multipleBirthBoolean
			//		multipleBirthInteger
			//	photo
			//	contact
			//		relationship
			//		name
			//		telecom
			//		address
			//		gender
			//		organization
			//		period
			//	communication
			//		language
			//		preferred
			//	generalPractitioner
			//	managingOrganization
			//	link
			//		other
			//		type

			return resource;
		}
		catch(Exception e) {
			return null;
		}
	}
}
