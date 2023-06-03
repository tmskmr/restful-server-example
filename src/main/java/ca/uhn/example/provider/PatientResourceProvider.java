package ca.uhn.example.provider;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Date;

import org.apache.ibatis.session.SqlSession;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.example.mybatis.MyPatientMapper;
import ca.uhn.example.common.Common;
import ca.uhn.example.converter.MyPatientConverter;
import ca.uhn.example.mybatis.MyPatient;
import ca.uhn.example.mybatis.MyPatientExample;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

/**
 * This is a resource provider which stores Patient resources in memory using a HashMap. This is obviously not a production-ready solution for many reasons,
 * but it is useful to help illustrate how to build a fully-functional server.
 */
public class PatientResourceProvider implements IResourceProvider {
	private static final String JP_KanaSortSP = "jp-patient-kanasort-sp";

	/**
	 * The getResourceType method comes from IResourceProvider, and must be overridden to indicate what type of resource this provider supplies.
	 */
	@Override
	public Class<Patient> getResourceType() {
		return Patient.class;
	}

	/**
	 * Patientリソースの検索を行う
	 *
	 * @param theActive
	 * @param theName
	 * @param theFamily
	 * @param theGiven
	 * @param theBirthDate
	 * @param theGender
	 * @param theTelecom
	 * @param theAddress
	 * @return
	 */
	@Search()
	public List<Patient> search(
			@OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam theIdentifier,
			@OptionalParam(name = Patient.SP_NAME) StringParam theName,
			@OptionalParam(name = Patient.SP_FAMILY) StringParam theFamily,
			@OptionalParam(name = Patient.SP_GIVEN) StringParam theGiven,
			@OptionalParam(name = Patient.SP_BIRTHDATE) DateRangeParam theBirthDate,
			@OptionalParam(name = Patient.SP_GENDER) TokenParam theGender,
			@OptionalParam(name = Patient.SP_TELECOM) TokenParam theTelecom,
			@OptionalParam(name = Patient.SP_ADDRESS) StringParam theAddress,
			@Sort SortSpec theSort,
			@Count Integer theCount
			) {

		List<Patient> list = new ArrayList<Patient>();

		SqlSession session = null;
		try {
			session = Common.OpenSqlSession();
			if (session == null)
				throw new InternalErrorException("DB Session open failed.");

			List<MyPatient> tables = selectMyPatient(session, theIdentifier, theName, theFamily, theGiven, theBirthDate,
					theGender, theTelecom, theAddress );

			for (MyPatient table : tables) {
				if (theCount != null && list.size() >= theCount)
					break;

				Patient resource = MyPatientConverter.CreatePatientResource(session, table);
				if (resource != null)
					list.add(resource);
			}

			if (theSort != null) {
				sortResourceList(list, theSort);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new InternalErrorException(e);
		}
		finally{
			if (session != null)
				session.close();
		}

		return list;
	}

	public List<MyPatient> selectMyPatient(SqlSession session,
			TokenParam theIdentifier,
			StringParam theName,
			StringParam theFamily,
			StringParam theGiven,
			DateRangeParam theBirthDate,
			TokenParam theGender,
			TokenParam theTelecom,
			StringParam theAddress  ) {

		// MY_PATIENTを検索して Patient リソースに変換し、listに追加する
		MyPatientMapper mapper = session.getMapper(MyPatientMapper.class);
		MyPatientExample example = new MyPatientExample();
		MyPatientExample.Criteria criteria = example.createCriteria();

		if (theIdentifier != null) {
			String system = theIdentifier.getSystem();
			if (system != null && !system.equals(Common.GetNSPatient()))
				throw new ResourceNotFoundException("Not recognized identifier system:" + system);

			String value = theIdentifier.getValue();
			if (value != null)
				criteria.andPt_idEqualTo(value);
		}

		if (theFamily != null) {
			String family = theFamily.getValue();
			if (Common.IsFullKatakana(family)) {
				if (theFamily.isExact())
					criteria.andPt_family_sylEqualTo(family);
				else
					criteria.andPt_family_sylLike(family + "%");
			}
			else {
				if (theFamily.isExact())
					criteria.andPt_family_ideEqualTo(family);
				else
					criteria.andPt_family_ideLike(family + "%");
			}
		}

		if (theGiven != null) {
			String given = theGiven.getValue();
			if (Common.IsFullKatakana(given)) {
				if (theGiven.isExact())
					criteria.andPt_given_sylEqualTo(given);
				else
					criteria.andPt_given_sylLike(given + "%");
			}
			else {
				if (theGiven.isExact())
					criteria.andPt_given_ideEqualTo(given);
				else
					criteria.andPt_given_ideLike(given + "%");
			}
		}

		if (theBirthDate != null) {
			if (theBirthDate.getLowerBound() != null) {
				Date sdate = theBirthDate.getLowerBound().getValue();
				ParamPrefixEnum sprefix = theBirthDate.getLowerBound().getPrefix();
				if (sprefix == ParamPrefixEnum.GREATERTHAN_OR_EQUALS)
					criteria.andPt_birthdateGreaterThanOrEqualTo(sdate);
				else if (sprefix == ParamPrefixEnum.GREATERTHAN)
					criteria.andPt_birthdateGreaterThan(sdate);
				else if (sprefix == ParamPrefixEnum.EQUAL)
					criteria.andPt_birthdateEqualTo(sdate);
			}

			if (theBirthDate.getUpperBound() != null) {
				Date edate = theBirthDate.getUpperBound().getValue();
				ParamPrefixEnum eprefix = theBirthDate.getUpperBound().getPrefix();
				if (eprefix == ParamPrefixEnum.LESSTHAN_OR_EQUALS)
					criteria.andPt_birthdateLessThanOrEqualTo(edate);
				else if (eprefix == ParamPrefixEnum.LESSTHAN)
					criteria.andPt_birthdateLessThan(edate);
			}
		}

		if (theGender != null) {
			switch(theGender.getValue()) {
			case "male":
				criteria.andPt_genderEqualTo("M");
				break;
			case "female":
				criteria.andPt_genderEqualTo("F");
				break;
			case "other":
				criteria.andPt_genderEqualTo("O");
				break;
			default:
				throw new InvalidRequestException("Invalid gender value:" + theGender.getValue());
			}
		}

		if (theTelecom != null) {
			criteria.andPt_phoneEqualTo(theTelecom.getValue());
		}

		if (theAddress != null) {
			String address = theAddress.getValue();
			if (theAddress.isExact())
				criteria.andPt_addressEqualTo(address);
			else
				criteria.andPt_addressLike(address + "%");
		}

		return mapper.selectByExample(example);
	}

	/**
	 * Patientリソースのリストを指定されたソート条件でソートする
	 *
	 * @param list		ソート対象のリスト
	 * @param theSort	ソート条件
	 */
	private static void sortResourceList(List<Patient> list, SortSpec theSort) {
		// The name of the param to sort by
		String param = theSort.getParamName();

		// The sort order, or null
		int sign = theSort.getOrder() == SortOrderEnum.ASC ? 1 : -1;

		switch(param) {
		case Patient.SP_IDENTIFIER:
			Collections.sort(
					list,
					new Comparator<Patient>() {
						private String SAFESTR(Patient pat) {
							if (pat == null)
								return "";

							Identifier identifier = pat.getIdentifierFirstRep();
							if (identifier == null)
								return "";

							return identifier.toString();
						}

						@Override
						public int compare(Patient obj1, Patient obj2) {
							return SAFESTR(obj1).compareTo(SAFESTR(obj2))*sign;
						}
					}
				);
			break;
		case Patient.SP_BIRTHDATE:
			Collections.sort(
					list,
					new Comparator<Patient>() {
						private java.util.Date SAFESTR(Patient pat) {
							if (pat == null)
								return new java.util.Date(0);

							java.util.Date date = pat.getBirthDate();
							if (date == null)
								return new java.util.Date(0);

							return date;
						}

						@Override
						public int compare(Patient obj1, Patient obj2) {
							return SAFESTR(obj1).compareTo(SAFESTR(obj2))*sign;
					   }
				   }
			   );
			   break;
		   case Patient.SP_GENDER:
			   Collections.sort(
				   list,
				   new Comparator<Patient>() {
					   private String SAFESTR(Patient pat) {
						   if (pat == null)
							   return "";

						   Enumerations.AdministrativeGender gender = pat.getGender();
						   if (gender == null)
							   return "";

						   return gender.toString();
					   }

					   @Override
					   public int compare(Patient obj1, Patient obj2) {
						   return SAFESTR(obj1).compareTo(SAFESTR(obj2))*sign;
					   }
				   }
			   );
			   break;
		   case Patient.SP_FAMILY:
			   Collections.sort(
				   list,
				   new Comparator<Patient>() {
					   private String SAFESTR(Patient pat) {
						   if (pat == null)
							   return "";

						   HumanName name = pat.getNameFirstRep();
						   if (name == null)
							   return "";

						   String fami = name.getFamily();
						   if (fami == null)
							   return "";

						   return fami.toString();
					   }

					   @Override
					   public int compare(Patient obj1, Patient obj2) {
						   return SAFESTR(obj1).compareTo(SAFESTR(obj2))*sign;
					   }
				   }
			   );
			   break;
		   case Patient.SP_GIVEN:
			   Collections.sort(
				   list,
				   new Comparator<Patient>() {
					   private String SAFESTR(Patient pat) {
						   if (pat == null)
							   return "";

						   HumanName name = pat.getNameFirstRep();
						   if (name == null)
							   return "";

						   String given = name.getGivenAsSingleString();
						   if (given == null)
							   return "";

						   return given;
					   }

					   @Override
					   public int compare(Patient obj1, Patient obj2) {
						   return SAFESTR(obj1).compareTo(SAFESTR(obj2))*sign;
					   }
				   }
			   );
			   break;
		   case Patient.SP_TELECOM:
			   Collections.sort(
				   list,
				   new Comparator<Patient>() {
					   private String SAFESTR(Patient pat) {
						   if (pat == null)
							   return "";

						   ContactPoint telecom = pat.getTelecomFirstRep();
						   if (telecom == null)
							   return "";

						   return telecom.toString();
					   }

					   @Override
					   public int compare(Patient obj1, Patient obj2) {
						   return SAFESTR(obj1).compareTo(SAFESTR(obj2))*sign;
					   }
				   }
			   );
			   break;
		   case Patient.SP_ADDRESS:
			   Collections.sort(
				   list,
				   new Comparator<Patient>() {
					   private String SAFESTR(Patient pat) {
						   if (pat == null)
							   return "";

						   Address addr = pat.getAddressFirstRep();
						   if (addr == null)
							   return "";

						   String str = addr.getText();
						   if (str == null)
							   return "";

						   return str;
					   }

					   @Override
					   public int compare(Patient obj1, Patient obj2) {
						   return SAFESTR(obj1).compareTo(SAFESTR(obj2))*sign;
					   }
				   }
			   );
			   break;
		   case JP_KanaSortSP:
			   Collections.sort(
					   list,
					   new Comparator<Patient>() {
						   private String SAFESTR(Patient pat) {
							   if (pat == null)
								   return "";

							   List<HumanName> list = pat.getName();
							   for (HumanName name : list) {
								   Extension ext = name.getExtensionByUrl(Common.GetSDIso21090ENRepresentation());
								   if (ext != null && ((CodeType)ext.getValue()).getValueAsString().equals("SYL"))
									   return Common.SAFESTR(name.getText());
							   }

							   return "";
						   }

						   @Override
						   public int compare(Patient obj1, Patient obj2) {
							   return SAFESTR(obj1).compareTo(SAFESTR(obj2))*sign;
						   }
					   }
				   );
			   break;
		   default:
			   throw new NotImplementedOperationException("Not implemented sort:" + param);
		   }
	   }

	/**
	 * This is the "read" operation. The "@Read" annotation indicates that this method supports the read and/or vread operation.
	 * <p>
	 * Read operations take a single parameter annotated with the {@link IdParam} paramater, and should return a single resource instance.
	 * </p>
	 *
	 * @param theId
	 *            The read operation takes one parameter, which must be of type IdDt and must be annotated with the "@Read.IdParam" annotation.
	 * @return Returns a resource matching this identifier, or null if none exists.
	 */
	@Read(version = false)
	public Patient readPatient(@IdParam IdType theId) {
		SqlSession session = null;
		try {
			session = Common.OpenSqlSession();
			if (session == null)
				throw new InternalErrorException("DB Session open failed.");

			MyPatientMapper mapper = session.getMapper(MyPatientMapper.class);
			MyPatient table = mapper.selectByPrimaryKey(theId.getIdPart());

			if (table == null)
				throw new ResourceNotFoundException(theId);

			return MyPatientConverter.CreatePatientResource(session, table);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new InternalErrorException(e);
		}
		finally{
			if (session != null)
				session.close();
		}
	}
}
