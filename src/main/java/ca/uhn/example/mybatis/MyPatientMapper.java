package ca.uhn.example.mybatis;

import ca.uhn.example.mybatis.MyPatient;
import ca.uhn.example.mybatis.MyPatientExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MyPatientMapper {

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table public.my_patient
	 * @mbg.generated  Fri May 05 17:26:35 JST 2023
	 */
	long countByExample(MyPatientExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table public.my_patient
	 * @mbg.generated  Fri May 05 17:26:35 JST 2023
	 */
	int deleteByExample(MyPatientExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table public.my_patient
	 * @mbg.generated  Fri May 05 17:26:35 JST 2023
	 */
	int deleteByPrimaryKey(String pt_id);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table public.my_patient
	 * @mbg.generated  Fri May 05 17:26:35 JST 2023
	 */
	int insert(MyPatient row);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table public.my_patient
	 * @mbg.generated  Fri May 05 17:26:35 JST 2023
	 */
	int insertSelective(MyPatient row);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table public.my_patient
	 * @mbg.generated  Fri May 05 17:26:35 JST 2023
	 */
	List<MyPatient> selectByExample(MyPatientExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table public.my_patient
	 * @mbg.generated  Fri May 05 17:26:35 JST 2023
	 */
	MyPatient selectByPrimaryKey(String pt_id);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table public.my_patient
	 * @mbg.generated  Fri May 05 17:26:35 JST 2023
	 */
	int updateByExampleSelective(@Param("row") MyPatient row, @Param("example") MyPatientExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table public.my_patient
	 * @mbg.generated  Fri May 05 17:26:35 JST 2023
	 */
	int updateByExample(@Param("row") MyPatient row, @Param("example") MyPatientExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table public.my_patient
	 * @mbg.generated  Fri May 05 17:26:35 JST 2023
	 */
	int updateByPrimaryKeySelective(MyPatient row);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table public.my_patient
	 * @mbg.generated  Fri May 05 17:26:35 JST 2023
	 */
	int updateByPrimaryKey(MyPatient row);
}