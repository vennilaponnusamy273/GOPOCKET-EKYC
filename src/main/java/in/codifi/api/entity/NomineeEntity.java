package in.codifi.api.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import lombok.Getter;
import lombok.Setter;

@Entity(name = "tbl_nominee_details")
@Getter
@Setter
public class NomineeEntity extends CommonEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "application_id", nullable = false)
	private Long applicationId;

	@Column(name = "first_name")
	private String firstname;

	@Column(name = "last_name")
	private String lastname;

	@Column(name = "relationship")
	private String relationship;

	@Column(name = "dob")
	private String dateofbirth;

	@Column(name = "pancard")
	private String pancard;

	@Column(name = "mobile_number", length = 10)
	private long mobilenumber;

	@Column(name = "email_address")
	private String emailaddress;

	@Column(name = "address1")
	private String address1;

	@Column(name = "address2")
	private String address2;

	@Column(name = "pincode")
	private Long pincode;

	@Column(name = "state")
	private String state;

	@Transient
	private GuardianEntity guardianEntity;

}
