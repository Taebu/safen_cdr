package kr.co.cashq.safen_cdr;

import java.sql.Connection;
import java.sql.SQLException;

//import com.nostech.safen.SafeNo;

/**
 * safen_cmd_queue 테이블 관련 객체
 * @author pgs
 *
 */
public class Safen_cmd_queue {
	
	boolean is_realcode=false;
	boolean is_userpt=false;


	/**
	 * safen_cmd_queue 테이블의 데이터를 처리하기 위한 주요한 처리를 수행한다.
	 */
	public static void doMainProcess() {
		Connection con = DBConn.getConnection();

		if (con != null) {
			MyDataObject dao = new MyDataObject();
			MyDataObject dao2 = new MyDataObject();
			MyDataObject dao3 = new MyDataObject();
			MyDataObject dao4 = new MyDataObject();
			MyDataObject dao5 = new MyDataObject();

			StringBuilder sb = new StringBuilder();

			//sb.append("select exists(select 1 from safen_cdr) a");
			//sb.append("select * from safen_cdr limit 1");
			sb.append("select * from safen_cdr where seq<192 limit 1;");
			
			try {
				dao.openPstmt(sb.toString());

				dao.setRs(dao.pstmt().executeQuery());

				if (dao.rs().next()) {
					
					SAFEN_CDR.heart_beat = 1;
					Boolean chk_seq=dao.rs().getInt("seq")>0;

					if (chk_seq) {
						StringBuilder sb2 = new StringBuilder();
						StringBuilder sb5 = new StringBuilder();
						String hist_table = DBConn.isExistTableYYYYMM();
						int resultCnt2 = 0;
						/*
						*/
						/**
						*   select conn_sdt,service_sdt,conn_edt,status_cd from safen_cdr;
							
							public static int set_TB_CALL_LOG(
							String status_cd, 
							String conn_sdt, 
							String conn_edt,
							String service_sdt,
							String safen,
							String safen_in,
							String safen_out,
							String calllog_rec_file) 
						*/
						String status_cd=dao.rs().getString("status_cd")==null?"":dao.rs().getString("status_cd");
						String conn_sdt=dao.rs().getString("conn_sdt")==null?"":dao.rs().getString("conn_sdt");
						String conn_edt=dao.rs().getString("conn_edt")==null?"":dao.rs().getString("conn_edt");
						String service_sdt=dao.rs().getString("service_sdt")==null?"":dao.rs().getString("service_sdt");
						String safen=dao.rs().getString("safen")==null?"":dao.rs().getString("safen");
						String safen_in=dao.rs().getString("safen_in")==null?"":dao.rs().getString("safen_in");
						String safen_out=dao.rs().getString("safen_out")==null?"":dao.rs().getString("safen_out");
						String calllog_rec_file=dao.rs().getString("calllog_rec_file")==null?"":dao.rs().getString("calllog_rec_file");
						
						/* cashq.TB_CALL_LOG에 세팅합니다. */
						int tcl_seq = set_TB_CALL_LOG(status_cd, conn_sdt,conn_edt, service_sdt, safen,safen_in,safen_out,calllog_rec_file);
						
						/* cashq.store.callcnt 갱신*/
						update_stcall(safen_in);
						
						/* 4-2. Store Info 조회 */
						String[] store_info=getStoreInfo(safen_in);
						/**
						* store_info[0] = name, 상점이름 
						* store_info[1] = pre_pay, 골드,실버,캐시큐,일반,PRQ 
						* store_info[2] = biz_code, 비즈코드 
						* store_info[3] = seq, 상점고유번호 
						* store_info[4] = type, 상점타입(예, 치킨,피자 등등의 코드) 
						*/

						/* 4-3. Event Info 조회*/
						String[] event_info=getEventCodeInfo(store_info[2]);
						/**
						* event_info[0] = ev_st_dt, 이벤트 시작일
						* event_info[1] = ev_ed_dt, 이벤트 종료일
						* event_info[2] = eventcode, 이벤트 코드 
						* event_info[3] = cash, 캐시
						* event_info[4] = pt_day_cnt, 일정립제한 갯수
						* event_info[5] = pt_event_cnt, 이벤트코드 정립 제한 갯수
						* event_info[6] = ed_type, 이벤트코드 타입
						*/
						boolean chk_realcode=is_realcode(event_info[2],store_info[2]);


						sb2.append("insert into ");
						sb2.append(hist_table);
						/* 처리가 진행중인것은 포함하지 않는다. */
						sb2.append(" select * from safen_cdr where seq=?");
						
						// insert into safen_cmd_hist_201607 select * from
						// safen_cmd_queue where status_cd != ''
						dao2.openPstmt(sb2.toString());
						dao2.pstmt().setInt(1, dao.rs().getInt("seq"));

						resultCnt2 = dao2.pstmt().executeUpdate();
						if(resultCnt2!=1) {
							Utils.getLogger().warning(dao2.getWarning(resultCnt2,1));
							DBConn.latest_warning = "ErrPOS027";
						}

						// region 3 start --->
						StringBuilder sb3 = new StringBuilder();

						/* 처리가 진행중인것은 지우지 않는다. */
						sb3.append("delete from safen_cdr where seq=?");
				
						// insert into safen_cmd_hist_201607 select * from
						// safen_cmd_queue where status_cd != ''
						dao3.openPstmt(sb3.toString());
						dao3.pstmt().setInt(1, dao.rs().getInt("seq"));

						int resultCnt3 = dao3.pstmt().executeUpdate();
						if(resultCnt3!=1) {
							Utils.getLogger().warning(dao3.getWarning(resultCnt3,1));
							DBConn.latest_warning = "ErrPOS028";
						}
						// region 3 end <---

						// region 4 start --->
						/*
						StringBuilder sb4 = new StringBuilder();
						
						sb4.append("select * from safen_cdr limit 1");
						dao4.openPstmt(sb4.toString());

						dao4.setRs(dao4.pstmt().executeQuery());

						if (dao4.rs().next()) {
							int seq = dao4.rs().getInt("seq");
							String safen = dao4.rs().getString("safen");
							String safen_in = dao4.rs().getString("safen_in");
							doMapping(seq, safen, safen_in);
						}
						*/
						// region 4 end <---
					} else {
						//Utils.getLogger().info("chk_seq false log");
						if (!"".equals(Env.confirmSafen)) {
							// cmq_queue에는 없는 경우라면
							//SafeNo safeNo = new SafeNo();
							String retCode = "";
							try {
								//retCode = safeNo.SafeNoAsk(Env.getInstance().CORP_CODE,Env.confirmSafen);
							} catch (Exception e) {
								Utils.getLogger().warning(e.getMessage());
								Utils.getLogger().warning(Utils.stack(e));
								DBConn.latest_warning = "ErrPOS029";
							}

							if (-1 < retCode.indexOf(Env.confirmSafen_in)) {
								/* retCode = "01040421182,01040421182" 와 같은 형태로 리턴되는 식임 */
								Utils.getLogger().info(
										"OK 착신연결성공" + Env.confirmSafen + "->"
												+ Env.confirmSafen_in);
							} else {// 취소된 경우 recCode = "E401"이 리턴됨
								if (Env.NULL_TEL_NUMBER
										.equals(Env.confirmSafen_in)
										&& "E401".equals(retCode)) {
									Utils.getLogger().info(
											"OK 착신취소성공" + Env.confirmSafen
													+ ", retCode:[" + retCode
													+ "]");
								} else {
									Utils.getLogger().warning(
											"Error! " + Env.confirmSafen + "->"
													+ Env.confirmSafen_in
													+ "? retCode:[" + retCode
													+ "]");
									DBConn.latest_warning = "ErrPOS030";
								}
							}

							Env.confirmSafen = "";
						}
					}
				}				
			} catch (SQLException e) {
				Utils.getLogger().warning(e.getMessage());
				DBConn.latest_warning = "ErrPOS031";
				e.printStackTrace();
			} catch (Exception e) {
				Utils.getLogger().warning(e.getMessage());
				Utils.getLogger().warning(Utils.stack(e));
				DBConn.latest_warning = "ErrPOS032";
			}
			finally {
				dao.closePstmt();
				dao2.closePstmt();
				dao3.closePstmt();
				dao4.closePstmt();
				dao5.closePstmt();
			}
			
			//콜로그 마스터 정보의 레코드 1개를 갱신을 시도한다.
			//Safen_master.doWark2();
		}
	}

	/**
	 * 취소시는 safen_in010에 "1234567890"을 넣어야 함. 리턴코드4자리에 따른 의미
	 * 
	 * 0000:성공 처리(인증서버에서 요청 처리가 성공.) E101:Network 장애(인증서버와 연결 실패.) E102:System
	 * 장애(인증서버의 일시적 장애. 재시도 요망.) E201:제휴사 인증 실패(유효한 제휴사 코드가 아님.) E202:유효 기간
	 * 만료(제휴사와의 계약기간 만료.) E301:안심 번호 소진(유효한 안심번호 자원이 없음.) E401:Data Not
	 * Found(요청한 Data와 일치하는 Data가 없음.) E402:Data Overlap(요청한 Data가 이미 존재함.)
	 * E501:전문 오류(전문 공통부 혹은 본문의 Data가 비정상일 경우.) E502:전화 번호(오류 요청한 착신번호가 맵핑불가 번호일
	 * 경우.)
	 */
	private static String doMapping(int seq, String safen0504,
			String safen_in010) {

		String corpCode = Env.getInstance().CORP_CODE;
		String safeNum = null;
		String telNum1 = null;// "1234567890";
		String newNum1 = null;
		String telNum2 = null;
		String newNum2 = null;

		int mapping_option = 0;
		if (Env.NULL_TEL_NUMBER.equals(safen_in010)) {
			// 취소
			mapping_option = 2;

			String safen_in = getSafenInBySafen(safen0504);

			safeNum = safen0504;
			telNum1 = safen_in;
			newNum1 = Env.NULL_TEL_NUMBER;// "1234567890";;
			telNum2 = safen_in;
			newNum2 = Env.NULL_TEL_NUMBER;
		} else {
			// 등록 Create
			mapping_option = 1;
			safeNum = safen0504;
			telNum1 = Env.NULL_TEL_NUMBER;// "1234567890";
			newNum1 = safen_in010;
			telNum2 = Env.NULL_TEL_NUMBER;
			newNum2 = safen_in010;
		}

		// String groupCode = "anpr_1";
		String groupCode = "grp_1";
		
		groupCode = Safen_master.getGroupCode(safen0504);

		String reserved1 = "";
		String reserved2 = "";
		String retCode = "";

		//SafeNo safeNo = new SafeNo();

		try {
			update_cmd_queue(seq, safen0504, safen_in010, mapping_option, "");
			//retCode = safeNo.SafeNoMod(corpCode, safeNum, telNum1, newNum1,telNum2, newNum2, groupCode, reserved1, reserved2);
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS033";
		}

		// 후처리
		if ("0000".equals(retCode)) {
			Safen_master.update_safen_master(safen0504, safen_in010,
					mapping_option);

			Env.confirmSafen = safen0504;
			Env.confirmSafen_in = safen_in010;// 취소인경우는 1234567890 임

		}
		update_cmd_queue(seq, safen0504, safen_in010, mapping_option, retCode);

		return retCode;
	}

	/**
	 * 안심번호테이블을 갱신한다. 단, 이때 retCode가 공백이면 status_cd를 i로 넣고 진행중으로만 마킹하고 프로세스를
	 * 종료한다. retCode가 "0000"(성공)인경우에는 status_cd값을 "s"로 그렇지 않은 경우에는 "e"로 셋팅한 후 큐를
	 * 지우고 로그로 보낸다. 
	 * @param safen0504
	 * @param safen_in010
	 * @param mapping_option
	 * @param retCode
	 */
	private static void update_cmd_queue(int seq, String safen0504,
			String safen_in010, int mapping_option, String retCode) {

		MyDataObject dao = new MyDataObject();
		MyDataObject dao2 = new MyDataObject();
		MyDataObject dao3 = new MyDataObject();
		
		try {
			if ("".equals(retCode)) {
				StringBuilder sb = new StringBuilder();
				sb.append("update safen_cmd_queue set status_cd=? where seq=?");

				// status_cd 컬럼을 "i"<진행중>상태로 바꾼다.
				dao.openPstmt(sb.toString());

				dao.pstmt().setString(1, "i");
				dao.pstmt().setInt(2, seq);

				int cnt = dao.pstmt().executeUpdate();
				if(cnt!=1) {
					Utils.getLogger().warning(dao.getWarning(cnt,1));
					DBConn.latest_warning = "ErrPOS034";
				}

				dao.tryClose();

			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("update safen_cmd_queue set status_cd=?,result_cd=? where seq=?");

				if ("0000".equals(retCode)) {
					// status_cd 컬럼을 "s"<성공>상태로 바꾼다.
					
					dao2.openPstmt(sb.toString());

					dao2.pstmt().setString(1, "s");
					dao2.pstmt().setString(2, retCode);
					dao2.pstmt().setInt(3, seq);

					int cnt = dao2.pstmt().executeUpdate();
					if(cnt!=1) {
						Utils.getLogger().warning(dao2.getWarning(cnt,1));
						DBConn.latest_warning = "ErrPOS035";
					}

					dao2.tryClose();
				} else {
					// status_cd 컬럼을 "e"<오류>상태로 바꾼다.
					dao3.openPstmt(sb.toString());

					dao3.pstmt().setString(1, "e");
					dao3.pstmt().setString(2, retCode);
					dao3.pstmt().setInt(3, seq);

					int cnt = dao3.pstmt().executeUpdate();
					if(cnt!=1) {
						Utils.getLogger().warning(dao3.getWarning(cnt,1));
						DBConn.latest_warning = "ErrPOS036";
					}					
					dao3.tryClose();
				}
			}
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS037";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS038";
			Utils.getLogger().warning(Utils.stack(e));
		}
		finally {
			dao.closePstmt();
			dao2.closePstmt();
			dao3.closePstmt();
		}
	}

	/**
	 * 마스터 테이블에서 안심번호에 따른 착신번호를 리턴한다.
	 * @param safen0504
	 * @return
	 */
	private static String getSafenInBySafen(String safen0504) {
		String retVal = "";
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("select safen_in from safen_master where safen = ?");
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, safen0504);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				retVal = dao.rs().getString("safen_in");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return retVal;
	}


	
	/**
	 * TB_CALL_LOG에 추가한다.
	 * @param String status_cd	콜로그 상태 코드
	 * @param String conn_sdt	콜로그 시작시간
	 * @param String conn_edt	콜로그 종료시간
	 * @param String service_sdt	콜로그 제공시간
	 * @param String safen	안심번호
	 * @param String safen_in
	 * @param String safen_out
	 * @param String calllog_rec_file
	 * @return
	 */
	public static int set_TB_CALL_LOG(String status_cd, 
		String conn_sdt, String conn_edt,String service_sdt,
		String safen,String safen_in,String safen_out,
		String calllog_rec_file) 
	{
		boolean retVal = false;
		int last_id = 0;
		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		MyDataObject dao2 = new MyDataObject();
		/*
		Table: TB_CALL_LOG
		Create Table: CREATE TABLE `TB_CALL_LOG` (
		  `seq` int(11) NOT NULL AUTO_INCREMENT,
		  `SVC_ID` varchar(4) DEFAULT NULL,
		  `START_DT` datetime DEFAULT NULL,
		  `END_DT` datetime DEFAULT NULL,
		  `CALLED_HANGUP_DT` datetime DEFAULT NULL,
		  `CALLER_NUM` varchar(16) DEFAULT NULL,
		  `CALLED_NUM` varchar(16) DEFAULT NULL,
		  `VIRTUAL_NUM` varchar(16) DEFAULT NULL,
		  `REASON_CD` varchar(16) DEFAULT NULL,
		  `REG_DT` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
		  `userfield` varchar(255) DEFAULT NULL,
		  `biz_code` varchar(20) DEFAULT NULL,
		  `po_status` enum('0','1','2','3','4','5','6','99') NOT NULL DEFAULT '0',
		  PRIMARY KEY (`seq`)
		) ENGINE=MyISAM AUTO_INCREMENT=3143353 DEFAULT CHARSET=utf8
		1 row in set (0.00 sec)

		ERROR:
		*/

		sb.append("INSERT INTO `cashq`.`TB_CALL_LOG` SET ");
		sb.append("SVC_ID='81',");
		sb.append("START_DT=?,");
		sb.append("END_DT=?,");
		sb.append("CALLED_HANGUP_DT=?,");
		sb.append("VIRTUAL_NUM=?,");
		sb.append("CALLED_NUM=?,");
		sb.append("CALLER_NUM=?,");
		sb.append("userfield=?,");
		sb.append("REASON_CD=?;");

		/*
		sb.append("insert into cashq.site_push_log set "
				+ "stype='SMS', biz_code='ANP', caller=?, called=?, wr_subject=?, regdate=now(), result=''");
		*/
		try {
			dao.openPstmt(sb.toString());

			Utils.getLogger().warning(sb.toString());

			if ("1".equals(status_cd)) {
			/* GCM LOG 발생*/
			set_stgcm(safen, safen_in);

			/* 통화성공 */
			dao.pstmt().setString(1, conn_sdt);
			dao.pstmt().setString(2, conn_edt);
			dao.pstmt().setString(3, service_sdt);
			dao.pstmt().setString(4, safen);
			dao.pstmt().setString(5, safen_in);
			dao.pstmt().setString(6, safen_out);
			dao.pstmt().setString(7, calllog_rec_file);
			dao.pstmt().setString(8, status_cd);
			}else{
			/* 통화실패*/
			dao.pstmt().setString(1, conn_sdt);
			dao.pstmt().setString(2, conn_edt);
			dao.pstmt().setString(3, "1970-01-01 09:00:00");
			dao.pstmt().setString(4, safen);
			dao.pstmt().setString(5, safen_in);
			dao.pstmt().setString(6, safen_out);
			dao.pstmt().setString(7, calllog_rec_file);
			dao.pstmt().setString(8, status_cd);
			}

			dao.pstmt().executeUpdate();


			sb2.append("select LAST_INSERT_ID() last_id;");
			dao2.openPstmt(sb2.toString());
			dao2.setRs(dao2.pstmt().executeQuery());
			
			if (dao2.rs().next()) {
				last_id = dao2.rs().getInt("last_id");
			}
			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
			grant all privileges on cashq.site_push_log to sktl@"%" identified by 'sktl@9495';
			grant all privileges on cashq.site_push_log to sktl@"localhost" identified by 'sktl@9495';
			 */
			 
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} catch (NullPointerException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS099";
		} finally {
			dao.closePstmt();
			dao2.closePstmt();
		}

		return last_id;
	}


	/**
	 * 0507_point에 추가한다.
	 * @param String status_cd	콜로그 상태 코드
	 * @param String conn_sdt	콜로그 시작시간
	 * @param String conn_edt	콜로그 종료시간
	 * @param String service_sdt	콜로그 제공시간
	 * @param String safen	
	 * @param String safen_in
	 * @param String safen_out
	 * @param String calllog_rec_file
	 * @return
	 */
	public static int set_0507_point(String status_cd, 
		String conn_sdt, String conn_edt,String service_sdt,
		String safen,String safen_in,String safen_out,
		String calllog_rec_file) 
	{

		boolean retVal = false;
		int last_id = 0;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("insert into `cashq`.`0507_point` set ");
		sb.append("mb_hp=?,");
		sb.append("store_name=?,");
		sb.append("point='2000',");
		sb.append("hangup_time=?,");
		sb.append("biz_code='?',");
		sb.append("call_hangup_dt='?',");
		sb.append("ev_st_dt='?',");
		sb.append("ev_ed_dt='?',");
		sb.append("eventcode='?',");
		sb.append("mb_id='?',");
		sb.append("certi_code='?',");
		sb.append("insdate='?',");
		sb.append("st_dt='?',");
		sb.append("ed_dt='?',");
		sb.append("tcl_seq=?,");
		sb.append("store_seq=?,");
		sb.append("moddate='?',");
		sb.append("accdate='?' ");

		/*
		sb.append("insert into cashq.site_push_log set "
				+ "stype='SMS', biz_code='ANP', caller=?, called=?, wr_subject=?, regdate=now(), result=''");
		*/
		try {
			dao.openPstmt(sb.toString());

			if ("1".equals(status_cd)) {
			/* 통화성공 */
			dao.pstmt().setString(1, conn_sdt);
			dao.pstmt().setString(2, conn_edt);
			dao.pstmt().setString(3, service_sdt);
			dao.pstmt().setString(4, safen);
			dao.pstmt().setString(5, safen_in);
			dao.pstmt().setString(6, safen_out);
			dao.pstmt().setString(7, calllog_rec_file);
			dao.pstmt().setString(8, status_cd);
			}else{
			/* 통화실패*/
			dao.pstmt().setString(1, conn_sdt);
			dao.pstmt().setString(2, conn_edt);
			dao.pstmt().setString(3, "1970-01-01 09:00:00");
			dao.pstmt().setString(4, safen);
			dao.pstmt().setString(5, safen_in);
			dao.pstmt().setString(6, safen_out);
			dao.pstmt().setString(7, calllog_rec_file);
			dao.pstmt().setString(8, status_cd);
			}

			dao.pstmt().executeQuery();
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
			grant all privileges on cashq.site_push_log to sktl@"%" identified by 'sktl@9495';
			grant all privileges on cashq.site_push_log to sktl@"localhost" identified by 'sktl@9495';
			 */
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
		}
		return last_id;
	}



	
	/**
	 * cdr 에 추가한다.
	 * @param String status_cd	콜로그 상태 코드
	 * @param String conn_sdt	콜로그 시작시간
	 * @param String conn_edt	콜로그 종료시간
	 * @param String service_sdt	콜로그 제공시간
	 * @param String safen	
	 * @param String safen_in
	 * @param String safen_out
	 * @param String calllog_rec_file
	 * @return
	 */
	public static int set_cdr(String status_cd, 
		String conn_sdt, String conn_edt,String service_sdt,
		String safen,String safen_in,String safen_out,
		String calllog_rec_file) 
	{
		boolean retVal = false;
		int last_id = 0;
		StringBuilder sb = new StringBuilder();
		MyDataObject dao = new MyDataObject();
		sb.append("INSERT INTO `asteriskcdrdb`.`cdr` SET ");
		sb.append("calldate=?,");
		sb.append("src=?,");
		sb.append("dst=?,");
		sb.append("duration=?,");
		sb.append("billsec=?,");
		sb.append("accountcode=?,");
		sb.append("uniqueid=?,");
		sb.append("userfield=?;");
		//Utils.getLogger().warning(sb.toString());


		/*
		sb.append("insert into cashq.site_push_log set "
				+ "stype='SMS', biz_code='ANP', caller=?, called=?, wr_subject=?, regdate=now(), result=''");
		*/
		try {
			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, dao.rs().getString("conn_sdt"));
			dao.pstmt().setString(2, dao.rs().getString("safen_in"));
			dao.pstmt().setString(3, dao.rs().getString("safen"));
			dao.pstmt().setString(4, dao.rs().getString("conn_sec"));
			dao.pstmt().setString(5, dao.rs().getString("service_sec"));
			dao.pstmt().setString(6, dao.rs().getString("safen_out"));
			dao.pstmt().setString(7, dao.rs().getString("unique_id"));
			dao.pstmt().setString(8, dao.rs().getString("rec_file_cd"));

			dao.pstmt().executeUpdate();
			retVal = true;
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS060";
			/* grant로 해당 사용자에 대한 권한을 주어 문제 해결이 가능하다.
			grant all privileges on cashq.site_push_log to sktl@"%" identified by 'sktl@9495';
			grant all privileges on cashq.site_push_log to sktl@"localhost" identified by 'sktl@9495';
			 */
		} catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS061";
		} finally {
			dao.closePstmt();
		}
		//return retVal;
		return last_id;
	}
	
	
	/**
	 * 상점 콜로그로 갱신한다.  retCode가 "0000"(성공)인경우에는 status_cd값을 "s"로 그렇지 않은 경우에는 "e"로 셋팅한 후 큐를
	 * 지우고 로그로 보낸다. 
	 * @param safen_in
	 * @param retCode
	 */
	private static void update_stcall(String safen_in) {

		MyDataObject dao = new MyDataObject();
		
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE `cashq`.`store` set callcnt=callcnt+1 where tel=?");

			// status_cd 컬럼을 "i"<진행중>상태로 바꾼다.
			dao.openPstmt(sb.toString());

			dao.pstmt().setString(1, safen_in);

			int cnt = dao.pstmt().executeUpdate();
			if(cnt!=1) {
				Utils.getLogger().warning(dao.getWarning(cnt,1));
				DBConn.latest_warning = "ErrPOS034";
			}

			dao.tryClose();


		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS037";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS038";
			Utils.getLogger().warning(Utils.stack(e));
		}
		finally {
			dao.closePstmt();
		}
	}


	/**
	 * 캐시큐 상점에서 안심번호에 따른 상점 정보를 리턴한다.
	 * @param safen
	 * @return
	 */
	private static String[] getStoreInfo(String safen) {
		String[] s = new String[5];
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("select name,pre_pay,biz_code,seq,type from `cashq`.`store` where tel= ?");
		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, safen);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				s[0] = dao.rs().getString("name");
				s[1] = dao.rs().getString("pre_pay");
				s[2] = dao.rs().getString("biz_code");
				s[3] = dao.rs().getString("seq");
				s[4] = dao.rs().getString("type");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return s;
	}

	/**
	 * 비즈코드에 따른 이벤트 코드 정보를 리턴한다.
	 * @param biz_code
	 * @return
	 */
	private static String[] getEventCodeInfo(String biz_code) {
		String[] s = new String[7];
		StringBuilder sb = new StringBuilder();

		MyDataObject dao = new MyDataObject();
		sb.append("SELECT ev_st_dt,ev_ed_dt,eventcode,");
		sb.append("cash,pt_day_cnt,pt_event_cnt,ed_type ");
		sb.append("FROM `cashq`.`point_event_dt` ");
		sb.append("WHERE biz_code=? and used='1' ");
		sb.append("order by seq desc limit 1;");

		try {
			dao.openPstmt(sb.toString());
			dao.pstmt().setString(1, biz_code);
			
			dao.setRs (dao.pstmt().executeQuery());

			if (dao.rs().next()) {
				s[0] = dao.rs().getString("ev_st_dt");
				s[1] = dao.rs().getString("ev_ed_dt");
				s[2] = dao.rs().getString("eventcode");
				s[3] = dao.rs().getString("cash");
				s[4] = dao.rs().getString("pt_day_cnt");
				s[4] = dao.rs().getString("pt_event_cnt");
				s[4] = dao.rs().getString("ed_type");
			}			
		} catch (SQLException e) {
			Utils.getLogger().warning(e.getMessage());
			DBConn.latest_warning = "ErrPOS039";
			e.printStackTrace();
		}
		catch (Exception e) {
			Utils.getLogger().warning(e.getMessage());
			Utils.getLogger().warning(Utils.stack(e));
			DBConn.latest_warning = "ErrPOS040";
		}
		finally {
			dao.closePstmt();
		}

		return s;
	}
	
	/**
	* is_realcode
	*/
	private static boolean is_realcode(String eventcode,String biz_code) {
		boolean is_code=false;

		String[] explode=eventcode.split("\\_");

		is_code=explode[0].equals(biz_code);
		return is_code;
	}
}
